package commands;

import managers.ConsoleManager;

import managers.CollectionManager;
import models.MusicBand;

/**
 * Команда для добавления элемента, если его значение превышает наибольший элемент
 *
 * @author Михаил
 */
public class AddIfMax extends Command {
    public AddIfMax(CollectionManager collectionManager, ConsoleManager consoleManager) {
        super("add_if_max", "добавить новый элемент в коллекцию, если его значение превышает значение наибольшего элемента этой коллекции", 0, collectionManager, consoleManager);
    }

    @Override
    public boolean run(String[] args) {
        if (!checkArgAmount(args)) {
            return true;
        }

        MusicBand bandToAdd = consoleManager.askMusicBand();
        for (MusicBand band : collectionManager.getCollection()) {
            if (band.compareTo(bandToAdd) > -1) {
                consoleManager.getTerminal().writer().println("Банда не добавлена");
                return true;
            }
        }
        collectionManager.addElement(bandToAdd);
        consoleManager.getTerminal().writer().println("Банда добавлена успешно");
        return true;
    }
}