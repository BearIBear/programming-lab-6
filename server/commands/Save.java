package server.commands;


import server.managers.CollectionManager;
import server.managers.FileManager;

/**
 * Команда для сохранения коллекции в файл
 *
 * @author Михаил
 */
public class Save extends Command {
    private FileManager fileManager;

    public Save(CollectionManager collectionManager, FileManager fileManager) {
        super("save", "сохранить коллекцию в файл", 0, collectionManager);
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