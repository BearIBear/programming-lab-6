import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jline.reader.Highlighter;
import org.jline.reader.History;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.impl.completer.AggregateCompleter;
import org.jline.reader.impl.completer.StringsCompleter;
import org.jline.reader.impl.history.DefaultHistory;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;

import client.managers.ConsoleManager;
import util.CommandPayload;
import util.CommandResult;
import util.Packet;

import org.jline.builtins.Completers.FileNameCompleter;
import org.apache.commons.lang3.SerializationUtils;

/**
 * Главный класс приложения, содержащий точку входа и инициализацию компонентов JLine и команд
 *
 * @author Михаил
 */
class MainClient {
    public static void main(String[] args) {
        UUID clientUUID = UUID.randomUUID();
        System.out.println("UUID Клиента: " + clientUUID);
        try (DatagramSocket clientSocket = new DatagramSocket()) {
            try {
                clientSocket.setSoTimeout(5000);
                Terminal terminal = TerminalBuilder.builder().system(true).build();
                History history = new DefaultHistory();

                ConsoleManager consoleManager = new ConsoleManager(terminal);

                System.out.println("Клиент запущен, пытаемся передать UUID");
                Packet sendPacket = new Packet(clientUUID, 1, 0, null);
                byte[] serializedPacket = SerializationUtils.serialize(sendPacket);
                DatagramPacket sendDatagramPacket = new DatagramPacket(serializedPacket, 1024, InetAddress.getLocalHost(), 3553);
                clientSocket.send(sendDatagramPacket);
                System.out.println("UUID отправлен успешно");

                byte[] receiveBuffer = new byte[1024];
                DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                
                for (int i = 0;; i++) {
                    try {
                        if (i > 0) {
                            System.out.println("Попытка переподключения: " + i);
                            clientSocket.send(sendDatagramPacket);
                        }
                        clientSocket.receive(receivePacket);
                        break;
                    } catch (SocketTimeoutException e) {
                        System.out.println("Получение команд провалилось...");
                    }
                }

                ArrayList<Packet> packets = new ArrayList<>();
                Packet receivedPacket = SerializationUtils.deserialize(receiveBuffer);
                packets.add(receivedPacket);
                for (byte i = 1; i < receivedPacket.getPacketsAmount(); i++) {
                    clientSocket.receive(receivePacket);
                    receivedPacket = SerializationUtils.deserialize(receiveBuffer);
                    packets.add(receivedPacket);
                }
                String[] commandNames = (String[]) Packet.restoreObject(packets);
                System.out.println("Имена команд получены успешно");

                String commands = "\\b(" + String.join("|", commandNames) + "|" + "exit" + ")\\b";
                final Pattern commandsPattern = Pattern.compile(commands, Pattern.CASE_INSENSITIVE);
                List<String> commandNamesList = Arrays.stream(commandNames).toList();

                receiveBuffer = new byte[1024];
                receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                clientSocket.receive(receivePacket);

                packets = new ArrayList<>();
                receivedPacket = SerializationUtils.deserialize(receiveBuffer);
                packets.add(receivedPacket);
                for (byte i = 1; i < receivedPacket.getPacketsAmount(); i++) {
                    clientSocket.receive(receivePacket);
                    receivedPacket = SerializationUtils.deserialize(receiveBuffer);
                    packets.add(receivedPacket);
                }
                String[] filesRaw = (String[]) Packet.restoreObject(packets);
                System.out.println("Имена файлов получены успешно");

                String files = String.join("|", filesRaw);
                files = files.replace(".", "\\.");
                files = files.replace("(", "\\(");
                files = files.replace(")", "\\)");
                files = "\\b(" + files + ")\\b";
                final Pattern filesPattern = Pattern.compile(files);

                Highlighter consoleHighlighter = new Highlighter() {
                    @Override
                    public AttributedString highlight(LineReader reader, String buffer) {
                        AttributedStringBuilder builder = new AttributedStringBuilder();
                        if (buffer.length() <= 1) {
                            return builder.append(buffer).toAttributedString();
                        }

                        Matcher matcherCommand = commandsPattern.matcher(buffer);
                        Matcher matcherFiles = filesPattern.matcher(buffer);

                        boolean resultCommand = matcherCommand.find();
                        boolean resultFile = matcherFiles.find();

                        if (!resultCommand && !resultFile) {
                            builder.append(buffer);
                            return builder.toAttributedString();
                        }

                        if (resultCommand) {
                            builder.append(buffer.substring(0, matcherCommand.start()));
                            builder.styled(
                                    AttributedStyle.BOLD.foreground(AttributedStyle.BLUE),
                                    buffer.substring(matcherCommand.start(), matcherCommand.end()));

                            if (!resultFile) {
                                builder.append(buffer.substring(matcherCommand.end()));
                                return builder.toAttributedString();
                            }
                        }

                        if (resultFile) {
                            int previousEnd;
                            if (!resultCommand) {
                                previousEnd = 0; 
                            } else {
                                previousEnd = matcherCommand.end();
                            }

                            if (previousEnd > matcherFiles.start()) {
                                try {
                                    matcherFiles.find();
                                    builder.append(buffer.substring(previousEnd, matcherFiles.start()));
                                } catch (IllegalStateException e) {
                                    return builder.append(buffer.substring(previousEnd)).toAttributedString();
                                }
                            } else {
                                builder.append(buffer.substring(previousEnd, matcherFiles.start()));
                            }

                            builder.styled(
                                    AttributedStyle.BOLD.foreground(AttributedStyle.YELLOW),
                                    buffer.substring(matcherFiles.start(), matcherFiles.end()));
                            builder.append(buffer.substring(matcherFiles.end())); 
                        }
                        return builder.toAttributedString();
                    }
                };
                
                AggregateCompleter dynamicCompleter = new AggregateCompleter(new StringsCompleter(commandNames), new FileNameCompleter());
                LineReader reader = LineReaderBuilder.builder()
                        .terminal(terminal)
                        .completer(dynamicCompleter)
                        .history(history)
                        .variable(LineReader.HISTORY_FILE, Paths.get("history.txt"))
                        .highlighter(consoleHighlighter)
                        .build();
                consoleManager.setReader(reader);

                // TODO: Разделить данный луп на:
                // 1. Клиентский, который просто считывает/проверяет данные, а потом печатает ответ сервера
                // 2. Серверный (Вынести в отдельный файл), который просто получает данные от данного лупа и что-то с ними делает

                while (true) {
                    String input = reader.readLine("> ");
                    String[] tokens = input.strip().split(" ");
                    String commandName = tokens[0];
                    if (commandNamesList.contains(commandName)) {
                        CommandPayload commandPayload = new CommandPayload(commandName, tokens, null);

                        if (commandName.contains("add") || commandName.contains("update")) {
                            commandPayload.setBand(consoleManager.askMusicBand());
                        }

                        packets = (ArrayList<Packet>) Packet.packObject(clientUUID, commandPayload);
                        Packet.clientSendPackets(clientSocket, packets, InetAddress.getLocalHost(), 3553);

                        receiveBuffer = new byte[1024];
                        receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                        clientSocket.receive(receivePacket);

                        packets = new ArrayList<>();
                        receivedPacket = SerializationUtils.deserialize(receiveBuffer);
                        packets.add(receivedPacket);
                        for (byte i = 1; i < receivedPacket.getPacketsAmount(); i++) {
                            clientSocket.receive(receivePacket);
                            receivedPacket = SerializationUtils.deserialize(receiveBuffer);
                            packets.add(receivedPacket);
                        }
                        CommandResult result = (CommandResult) Packet.restoreObject(packets);
                        terminal.writer().println(result.getMessage());
                    } else if (tokens[0].isBlank()) {} else if (tokens[0].equals("exit")) {
                        sendPacket = new Packet(clientUUID, 1, 0, null);
                        serializedPacket = SerializationUtils.serialize(sendPacket); 
                        sendDatagramPacket = new DatagramPacket(serializedPacket, 1024, InetAddress.getLocalHost(), 3553);
                        clientSocket.send(sendDatagramPacket);
                        break;
                    } else {
                        System.out.println("\u001B[31m" + input + " не распознано как имя команды. Введите help для справки." + "\u001B[0m");
                    }
                }
            } catch (IOException e) {
                System.err.println("Не удалось создать терминал: " + e.getMessage());
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }
}