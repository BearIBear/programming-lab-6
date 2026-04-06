package server.commands;

import client.managers.ConsoleManager;
import server.managers.CollectionManager;
import server.managers.FileManager;

/**
 * Команда для сохранения коллекции в файл
 *
 * @author Михаил
 */
public class Save extends Command {
    private FileManager fileManager;

    public Save(CollectionManager collectionManager, ConsoleManager consoleManager, FileManager fileManager) {
        super("save", "сохранить коллекцию в файл", 0, collectionManager, consoleManager);
        this.fileManager = fileManager;
    }

    @Override
    public boolean run(String[] args) {
        if (!checkArgAmount(args)) {
            return true;
        }
        fileManager.save(collectionManager);
        return true;
    }
}