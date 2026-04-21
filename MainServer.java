import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
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

import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.NonBlockingReader;

import util.*;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
    private static final Logger log = LogManager.getLogger(MainServer.class);
    public static void main(String[] args) {
        String fileName = System.getenv("INPUT_FILENAME");
        if (fileName == null || fileName.isBlank()) {
            fileName = "Data.json";
            log.warn("Env variable INPUT_FILENAME doesn't exist!");
            log.warn("Defaulting to file: " + fileName);
        }

        String[] fileNames = null;
        try {
            Stream<Path> pathStream = Files.list(Paths.get("./scripts/"));
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
            Terminal terminal = TerminalBuilder.builder().system(true).build();
            NonBlockingReader reader = terminal.reader();
            char[] commandString = new char[4];
            String serverCommand = "";
            int currentPosition = 0;
            char readCharacter = 0;
    
            try {
                Selector selector = Selector.open();
                DatagramChannel server = DatagramChannel.open();
                try {
                    server.bind(new InetSocketAddress(InetAddress.getByName("helios"), 3553));
                } catch (UnknownHostException e) {
                    log.error("Helios server creation failed, localhost one has been activated instead");
                    server.bind(new InetSocketAddress(3553));
                }
                server.configureBlocking(false);
                server.register(selector, SelectionKey.OP_READ);
                ByteBuffer buffer = ByteBuffer.allocate(1024);
                log.info("Server active, waiting for requests...");
                boolean working = true;
    
                while (working) {
                    int selectionAmount = selector.selectNow();
                    if (selectionAmount > 0) {
                        SelectionKey selectedKey = selector.selectedKeys().toArray(SelectionKey[]::new)[0];
                        selector.selectedKeys().remove(selectedKey);
                        log.info("Request received, attempting to work with it...");
                        SocketAddress clientAddress = server.receive(buffer);
                        buffer.flip();
                        byte[] receivedData = new byte[buffer.remaining()];
                        buffer.get(receivedData);
                        buffer.clear();
        
                        Packet receivedPacket = SerializationUtils.deserialize(receivedData);
                        UUID receivedUUID = receivedPacket.getClientUUID();
                        if (receivedPacket.isConnectionDefining()) {
                            if (userPackets.containsKey(receivedUUID)) {
                                log.info("Client disconnected with UUID: " + receivedUUID);
                            } else {
                                userPackets.put(receivedUUID, new ArrayList<>());
                                log.info("Client connected with UUID: " + receivedUUID);
        
                                ArrayList<Packet> packetsToSend = (ArrayList<Packet>) Packet.packObject(receivedUUID, commandNames);
                                Packet.serverSendPackets(server, packetsToSend, clientAddress);
        
                                packetsToSend = (ArrayList<Packet>) Packet.packObject(receivedUUID, fileNames);
                                Packet.serverSendPackets(server, packetsToSend, clientAddress);
                            }
                        } else {
                            ArrayList<Packet> packets = userPackets.get(receivedUUID); 
                            packets.add(receivedPacket);
                            log.info("Packet received from client with UUID: " + receivedUUID);
        
                            if (packets.size() == receivedPacket.getPacketsAmount()) {
                                CommandPayload commandPayload = (CommandPayload) Packet.restoreObject(packets);
                                CommandResult result = commandsList.get(commandPayload.getCommandName()).run(commandPayload.getArgs(), commandPayload.getBand());
                                commandManager.clearScriptFiles();
                                ArrayList<Packet> packetsToSend = (ArrayList<Packet>) Packet.packObject(receivedUUID, result);
                                Packet.serverSendPackets(server, packetsToSend, clientAddress);
                                userPackets.put(receivedUUID, new ArrayList<>());
                                log.info("Command " + commandPayload.getCommandName() + " executed from client with UUID: " + receivedUUID);
                            }
                        }
                    }

                    if (reader.available() > 0) {
                        readCharacter = (char) reader.read();
                        commandString[currentPosition++ % 4] = readCharacter;
                        System.out.print(readCharacter);
                        if (currentPosition % 4 == 0) {
                            System.out.println("");
                            serverCommand = String.valueOf(commandString);
                            if (serverCommand.equals("exit")) {
                                log.info("You are absolutely right! We shouldn't just save the collection — we should shut down the server");
                                commandsList.get("save").run(new String[1], null);
                                working = false;
                            } else if (serverCommand.equals("save")) {
                                log.info("Collection saved");
                                commandsList.get("save").run(new String[1], null);
                            }
                        }
                    }
                }


            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
