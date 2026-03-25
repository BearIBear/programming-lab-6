package commands;

import managers.ConsoleManager;

import managers.CollectionManager;

/**
 * Команда для вывода справки по доступным командам
 *
 * @author Михаил
 */
public class Help extends Command {
    public Help(CollectionManager collectionManager, ConsoleManager consoleManager) {
        super("help", "вывести справку по доступным командам", 0, collectionManager, consoleManager);
    }

    @Override
    public boolean run(String[] args) {
        if (!checkArgAmount(args)) {
            return true;
        }

        int max_length = 0;
        int padding = 5;
        var commandsList = commandManager.getCommandsList();
        for (String name : commandsList.keySet()) {
            max_length = Math.max(max_length, name.length());
        }
        for (String name : commandsList.keySet()) {
            System.out.println(name + " ".repeat(max_length + padding - name.length()) + commandsList.get(name).getDesc());
        }
        return true;
    }
}
