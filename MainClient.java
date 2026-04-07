import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.file.Paths;
import java.util.Arrays;
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

import org.jline.builtins.Completers.FileNameCompleter;

import org.apache.commons.lang3.SerializationUtils;

/**
 * Главный класс приложения, содержащий точку входа и инициализацию компонентов JLine и команд
 *
 * @author Михаил
 */
class MainClient {
    private final static UUID clientId = UUID.randomUUID();

    public static void main(String[] args) {
        try (DatagramSocket clientSocket = new DatagramSocket()) { // TODO: Сервер пусть будет на 3553
            try {
                Terminal terminal = TerminalBuilder.builder().system(true).build();
                History history = new DefaultHistory();

                ConsoleManager consoleManager = new ConsoleManager(terminal);

                byte[] serializedClientUUID = SerializationUtils.serialize(clientId);
                DatagramPacket sendPacket = new DatagramPacket(serializedClientUUID, serializedClientUUID.length, InetAddress.getLocalHost(), 3553);

                String[] commandNames = {}; //TODO: Сделать так, чтобы при первом подключении сервер отправлял клиенту список команд
                // СПИСОК КОМАНД ДОЛЖЕН БЫТЬ ОТСОРТИРОВАН!!!
                String commands = "\\b(" + String.join("|", commandNames) + ")\\b";
                final Pattern commandsPattern = Pattern.compile(commands, Pattern.CASE_INSENSITIVE);

                //TODO: Сделать так, чтобы при первом подключении сервер также отправил клиенту список файлов
                // String[] filesRaw = null;
                // try {
                //     Stream<Path> pathStream = Files.list(Paths.get("."));
                //     filesRaw = pathStream.filter(Files::isRegularFile).map(Path::getFileName).map(Path::toString).toArray(String[]::new);
                //     pathStream.close();
                // } catch (IOException e) {}

                String[] filesRaw = {};
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
                    if (Arrays.binarySearch(commandNames, tokens[0]) >= 0) {
                        
                    } else if (tokens[0].isBlank()) {} else if (tokens[0].equals("exit")) {
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