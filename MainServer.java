import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import util.*;

import org.apache.commons.lang3.SerializationUtils;

import server.commands.Add;
import server.commands.AddIfMax;
import server.commands.Clear;
import server.commands.Command;
import server.commands.CountLessThanDescription;
import server.commands.FilterContainsName;
import server.commands.FilterGreaterThanGenre;
import server.commands.Head;
import server.commands.Help;
import server.commands.Info;
import server.commands.RemoveById;
import server.commands.Save;
import server.commands.Script;
import server.commands.Show;
import server.commands.Update;
import server.managers.CollectionManager;
import server.managers.CommandManager;
import server.managers.FileManager;

public class MainServer {
    private static final HashMap<UUID, ArrayList<Packet>> userPackets = new HashMap<>();
    public static void main(String[] args) {
        String fileName = System.getenv("INPUT_FILENAME");
        if (fileName == null || fileName.isBlank()) {
            fileName = "Data.json";
            System.out.println("Переменной окружения INPUT_FILENAME нет");
            System.out.println("Файл по умолчанию: " + fileName);
        }

        String[] fileNames = null;
        try {
            Stream<Path> pathStream = Files.list(Paths.get("."));
            fileNames = pathStream.filter(Files::isRegularFile).map(Path::getFileName).map(Path::toString).toArray(String[]::new);
            pathStream.close();
        } catch (IOException e) {}
        
        CollectionManager collectionManager = new CollectionManager();
        FileManager fileManager = new FileManager(fileName);
        fileManager.load(collectionManager);

        CommandManager commandManager = new CommandManager();
        commandManager.register(new Help(collectionManager));
        commandManager.register(new Info(collectionManager));
        commandManager.register(new Add(collectionManager));
        commandManager.register(new Show(collectionManager));
        commandManager.register(new Save(collectionManager, fileManager));
        commandManager.register(new Clear(collectionManager));
        commandManager.register(new Update(collectionManager));
        commandManager.register(new RemoveById(collectionManager));
        commandManager.register(new Head(collectionManager));
        commandManager.register(new AddIfMax(collectionManager));
        commandManager.register(new CountLessThanDescription(collectionManager));
        commandManager.register(new Script(collectionManager));
        commandManager.register(new FilterContainsName(collectionManager));
        commandManager.register(new FilterGreaterThanGenre(collectionManager));
        Map<String, Command> commandsList = commandManager.getCommandsList();
        String[] commandNames = commandsList.keySet().toArray(String[]::new);

        try {
            Selector selector = Selector.open();
            DatagramChannel server = DatagramChannel.open();
            server.bind(new InetSocketAddress(3553));
            server.configureBlocking(false);
            server.register(selector, SelectionKey.OP_READ);
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            System.out.println("Сервер запущен, ожидаем запросы...");


            while (true) {
                selector.select();
                SelectionKey selectedKey = selector.selectedKeys().toArray(SelectionKey[]::new)[0];
                selector.selectedKeys().remove(selectedKey);
                System.out.println("Запрос получен, пытаемся обработать...");
                SocketAddress clientAddress = server.receive(buffer);
                buffer.flip();
                byte[] receivedData = new byte[buffer.remaining()];
                buffer.get(receivedData);
                buffer.clear();

                Packet receivedPacket = SerializationUtils.deserialize(receivedData);
                UUID receivedUUID = receivedPacket.getClientUUID();
                if (receivedPacket.isConnectionDefining()) {
                    if (userPackets.containsKey(receivedUUID)) {
                        System.out.println("Отключился клиент с UUID: " + receivedUUID);
                    } else {
                        userPackets.put(receivedUUID, new ArrayList<>());
                        System.out.println("Уникальный UUID добавлен: " + receivedUUID);

                        ArrayList<Packet> packetsToSend = (ArrayList<Packet>) Packet.packObject(receivedUUID, commandNames);
                        Packet.serverSendPackets(server, packetsToSend, clientAddress);

                        packetsToSend = (ArrayList<Packet>) Packet.packObject(receivedUUID, fileNames);
                        Packet.serverSendPackets(server, packetsToSend, clientAddress);
                    }
                } else {
                    ArrayList<Packet> packets = userPackets.get(receivedUUID); 
                    packets.add(receivedPacket);
                    System.out.println("Пакет добавлен от UUID: " + receivedUUID);

                    if (packets.size() == receivedPacket.getPacketsAmount()) {
                        CommandPayload commandPayload = (CommandPayload) Packet.restoreObject(packets);
                        CommandResult result = commandsList.get(commandPayload.getCommandName()).run(commandPayload.getArgs(), commandPayload.getBand());
                        ArrayList<Packet> packetsToSend = (ArrayList<Packet>) Packet.packObject(receivedUUID, result);
                        Packet.serverSendPackets(server, packetsToSend, clientAddress);
                        userPackets.put(receivedUUID, new ArrayList<>());
                        System.out.println("Команда " + commandPayload.getCommandName() + " обработана от UUID: " + receivedUUID);
                    }


                    


                    // CommandPayload commandPayload = SerializationUtils.deserialize(receivedPacket.getActualData());
                    // CommandResult result = commandsList.get(commandPayload.getCommandName()).run(commandPayload.getArgs(), commandPayload.getBand());
                    // byte[] serializedCommandResult = SerializationUtils.serialize(result);
                    // sendBuffer = ByteBuffer.wrap(serializedCommandResult);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
