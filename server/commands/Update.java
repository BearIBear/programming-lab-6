package server.commands;

import java.util.PriorityQueue;


import models.MusicBand;
import server.managers.CollectionManager;

/**
 * Команда для обновления значения элемента коллекции, id которого равен заданному
 *
 * @author Михаил
 */
public class Update extends Command {
    public Update(CollectionManager collectionManager) {
        super("update", "обновить значение элемента коллекции, id которого равен заданному", 1, collectionManager);
    }

    @Override
    public boolean run(String[] args) {
        args = Command.RemoveEmptyElements(args);
        if (!checkArgAmount(args)) {
            return true;
        }
        int id = Integer.parseInt(args[1]); 
        PriorityQueue<MusicBand> musicBands = collectionManager.getCollection();
        for (MusicBand musicBand : musicBands) {
            if (musicBand.getId() == id) {
                collectionManager.removeElement(id);
                MusicBand bandToInsert = consoleManager.askMusicBand();
                bandToInsert.setId(id);
                collectionManager.addElement(bandToInsert);
                return true;
            }
        }
        consoleManager.getTerminal().writer().println("\u001B[31m" + this.name + " : Элемент с id = " + id + " не найден" + "\u001B[0m");
        return true;
    }
}