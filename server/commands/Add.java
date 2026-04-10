package server.commands;


import models.MusicBand;
import server.managers.CollectionManager;
import util.CommandResult;

/**
 * Команда для добавления нового элемента в коллекцию
 *
 * @author Михаил
 */
public class Add extends Command {
    public Add(CollectionManager collectionManager) {
        super("add", "добавить новый элемент в коллекцию", 0, collectionManager);
    }

    @Override
    public CommandResult run(String[] args, MusicBand band) {
        CommandResult commandResult = checkArgAmount(args);
        if (!commandResult.isContinueFlag()) {
            return commandResult;
        }

        collectionManager.addElement(band);
        commandResult.setMessage("Банда добавлена успешно");
        return commandResult;
    }
}