package server.commands;


import models.MusicBand;
import server.managers.CollectionManager;
import util.CommandResult;

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
    public CommandResult run(String[] args, MusicBand band) {
        CommandResult commandResult = checkArgAmount(args);
        if (!commandResult.isContinueFlag()) {

            return commandResult;
        }
        collectionManager.clearCollection();
        CollectionManager.setNextId(1);
        return commandResult;
    }
}