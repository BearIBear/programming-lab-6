package server.commands;


import models.MusicBand;
import server.managers.CollectionManager;

/**
 * Команда для очистки коллекции
 *
 * @author Михаил
 */
public class Clear extends Command {
    public Clear(CollectionManager collectionManager) {
        super("clear", "очистить коллекцию", 0, collectionManager);
    }

    @Override
    public boolean run(String[] args) {
        if (!checkArgAmount(args)) {
            return true;
        }
        collectionManager.clearCollection();
        MusicBand.setNextId(1);
        return true;
    }
}