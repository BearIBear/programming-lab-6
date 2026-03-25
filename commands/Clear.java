package commands;

import managers.ConsoleManager;

import managers.CollectionManager;
import models.MusicBand;

/**
 * Команда для очистки коллекции
 *
 * @author Михаил
 */
public class Clear extends Command {
    public Clear(CollectionManager collectionManager, ConsoleManager consoleManager) {
        super("clear", "очистить коллекцию", 0, collectionManager, consoleManager);
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