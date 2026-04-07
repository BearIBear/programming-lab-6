package server.commands;


import models.MusicBand;
import server.managers.CollectionManager;

/**
 * Команда для добавления элемента, если его значение меньше наименьшего элемента коллекции
 *
 * @author Михаил
 */
public class AddIfMin extends Command {
    public AddIfMin(CollectionManager collectionManager) {
        super("add_if_min", "добавить новый элемент в коллекцию, если его значение превышает значение наибольшего элемента этой коллекции", 0, collectionManager);
    }

    @Override
    public boolean run(String[] args) {
        if (!checkArgAmount(args)) {
            return true;
        }

        MusicBand bandToAdd = consoleManager.askMusicBand();
        for (MusicBand band : collectionManager.getCollection()) {
            if (band.compareTo(bandToAdd) < 1) {
                return true;
            }
        }
        collectionManager.addElement(bandToAdd);
        return true;
    }
}