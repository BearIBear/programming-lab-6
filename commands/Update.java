package commands;

import managers.ConsoleManager;

import java.util.PriorityQueue;
import java.util.Scanner;

import managers.CollectionManager;
import managers.InputManager;
import models.MusicBand;

/**
 * Команда для обновления значения элемента коллекции, id которого равен заданному
 *
 * @author Михаил
 */
public class Update extends Command {
    public Update(CollectionManager collectionManager, ConsoleManager consoleManager) {
        super("update", "обновить значение элемента коллекции, id которого равен заданному", 1, collectionManager, consoleManager);
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
                MusicBand bandToInsert = new InputManager(new Scanner(System.in, System.getProperty("sun.stdout.encoding", "UTF-8"))).askMusicBand();
                bandToInsert.setId(id);
                collectionManager.addElement(bandToInsert);
                return true;
            }
        }
        consoleManager.getTerminal().writer().println("\u001B[31m" + this.name + " : Элемент с id = " + id + " не найден" + "\u001B[0m");
        return true;
    }
}