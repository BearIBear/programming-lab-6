package server.commands;

import client.managers.ConsoleManager;
import server.managers.CollectionManager;

/**
 * Команда для завершения работы программы (без сохранения в файл)
 *
 * @author Михаил
 */
public class Exit extends Command {
    public Exit(CollectionManager collectionManager, ConsoleManager consoleManager) {
        super("exit", "завершить программу (без сохранения в файл)", 0, collectionManager, consoleManager);
    }

    @Override
    public boolean run(String[] args) {
        if (!checkArgAmount(args)) {
            return true;
        }
        return false;
    }
}