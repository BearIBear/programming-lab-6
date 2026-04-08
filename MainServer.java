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
import java.util.List;
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
// import server.commands.Exit;
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
    private static final List<UUID> userUUIDs = new ArrayList<>();
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
        // commandManager.register(new Exit(collectionManager));
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
            ByteBuffer buffer = ByteBuffer.allocate(4096);
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
                if (receivedPacket.isConnectionDefining()) {
                    UUID receivedUUID = receivedPacket.getClientUUID();
                    if (userUUIDs.contains(receivedUUID)) {
                        System.out.println("Отключился клиент с UUID: " + receivedUUID);
                    } else {
                        userUUIDs.add(receivedUUID);
                        System.out.println("Уникальный UUID добавлен: " + receivedUUID);
                        byte[] serializedCommandNames = SerializationUtils.serialize(commandNames);
                        ByteBuffer sendBuffer = ByteBuffer.wrap(serializedCommandNames);
                        server.send(sendBuffer, clientAddress);
                        byte[] serializedFileNames = SerializationUtils.serialize(fileNames);
                        sendBuffer = ByteBuffer.wrap(serializedFileNames);
                        server.send(sendBuffer, clientAddress);
                    }
                } else {
                    // TODO: Сюда должен прилететь объект с именем команды (String), аргументами (String), объектом MusicBand
                    // Команды возвращают String, а потом отправляют его на сервер
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
