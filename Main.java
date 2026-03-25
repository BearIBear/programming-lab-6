import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

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
import org.jline.builtins.Completers.FileNameCompleter;

import commands.*;
import managers.*;

/**
 * Главный класс приложения, содержащий точку входа и инициализацию компонентов JLine и команд
 *
 * @author Михаил
 */
class Main {
    public static void main(String[] args) {
        String fileName = System.getenv("INPUT_FILENAME");
        if (fileName == null || fileName.isBlank()) {
            fileName = "Data.json";
            System.out.println("Переменной окружения INPUT_FILENAME нет");
            System.out.println("Файл по умолчанию: " + fileName);
        }
        
        CollectionManager collectionManager = new CollectionManager();
        FileManager fileManager = new FileManager(fileName);
        fileManager.load(collectionManager);

        try {
            Terminal terminal = TerminalBuilder.builder().system(true).build();
            History history = new DefaultHistory();

            ConsoleManager consoleManager = new ConsoleManager(terminal);
            CommandManager commandManager = new CommandManager();
            commandManager.register(new Help(collectionManager, consoleManager));
            commandManager.register(new Info(collectionManager, consoleManager));
            commandManager.register(new Add(collectionManager, consoleManager));
            commandManager.register(new Show(collectionManager, consoleManager));
            commandManager.register(new Save(collectionManager, consoleManager, fileManager));
            commandManager.register(new Clear(collectionManager, consoleManager));
            commandManager.register(new Exit(collectionManager, consoleManager));
            commandManager.register(new Update(collectionManager, consoleManager));
            commandManager.register(new RemoveById(collectionManager, consoleManager));
            commandManager.register(new Head(collectionManager, consoleManager));
            commandManager.register(new AddIfMax(collectionManager, consoleManager));
            commandManager.register(new CountLessThanDescription(collectionManager, consoleManager));
            commandManager.register(new Script(collectionManager, consoleManager));
            commandManager.register(new FilterContainsName(collectionManager, consoleManager));
            commandManager.register(new FilterGreaterThanGenre(collectionManager, consoleManager));
            Map<String, Command> commandsList = commandManager.getCommandsList();
            
            String[] commandNames = commandsList.keySet().toArray(String[]::new);
            String commands = "\\b(";
            for (int i = 0; i < commandNames.length - 1; i++) {
                commands += commandNames[i] + '|';
            }
            commands += commandNames[commandNames.length - 1] + ")\\b";
            final Pattern commandsPattern = Pattern.compile(commands, Pattern.CASE_INSENSITIVE);

            String[] filesRaw = null;
            try {
                Stream<Path> pathStream = Files.list(Paths.get("."));
                filesRaw = pathStream.filter(Files::isRegularFile).map(Path::getFileName).map(Path::toString).toArray(String[]::new);
                pathStream.close();
            } catch (IOException e) {}

            // String files = "\\b(";
            String files = "";
            for (int i = 0; i < filesRaw.length - 1; i++) {
                files += filesRaw[i] + '|';
            }
            // files += filesRaw[filesRaw.length - 1] + ")\\b";
            files += filesRaw[filesRaw.length - 1];
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
            
            var dynamicCompleter = new AggregateCompleter(new StringsCompleter(commandsList.keySet()), new FileNameCompleter());
            LineReader reader = LineReaderBuilder.builder()
                    .terminal(terminal)
                    .completer(dynamicCompleter)
                    .history(history)
                    .variable(LineReader.HISTORY_FILE, Paths.get("history.txt"))
                    .highlighter(consoleHighlighter)
                    .build();
            consoleManager.setReader(reader);

            boolean state = true;
            while (state) {
                String input = reader.readLine("> ");
                String[] tokens = input.strip().split(" ");
                if (commandsList.containsKey(tokens[0])) {
                    state = commandsList.get(tokens[0].toLowerCase()).run(tokens);
                    commandManager.setRecursionForcedExit(false);
                    commandManager.clearScriptFile();
                    consoleManager.getTerminal().flush();
                } else if (tokens[0].isBlank()) {} else {
                    System.out.println("\u001B[31m" + input + " не распознано как имя команды. Введите help для справки." + "\u001B[0m");
                }
            }
        } catch (IOException e) {
            System.err.println("Не удалось создать терминал: " + e.getMessage());
        }
    }
}