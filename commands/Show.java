package commands;

import managers.ConsoleManager;

import java.util.PriorityQueue;

import managers.CollectionManager;
import models.MusicBand;

/**
 * Команда для вывода всех элементов коллекции в строковом представлении
 *
 * @author Михаил
 */
public class Show extends Command {
    public Show(CollectionManager collectionManager, ConsoleManager consoleManager) {
        super("show", "вывести в стандартный поток вывода все элементы коллекции в строковом представлении", 0, collectionManager, consoleManager);
    }

    @Override
    public boolean run(String[] args) {
        if (!checkArgAmount(args)) {
            return true;
        }
        PriorityQueue<MusicBand> collection = collectionManager.getCollection();
        for (MusicBand musicBand : collection) {
            consoleManager.getTerminal().writer().println("ID группы: " + musicBand.getId());
            consoleManager.getTerminal().writer().println("Название группы: " + musicBand.getName());
            consoleManager.getTerminal().writer().println("Описание группы: " + musicBand.getDescription());
            consoleManager.getTerminal().writer().println("Фронтман: " + musicBand.getFrontMan());
            consoleManager.getTerminal().writer().println("Жанр: " + musicBand.getGenre());
            consoleManager.getTerminal().writer().println("Дата создания: " + musicBand.getCreationDate());
            consoleManager.getTerminal().writer().println("Количество участников: " + musicBand.getNumberOfParticipants());
            consoleManager.getTerminal().writer().println("Количество синглов: " + musicBand.getSinglesCount());
            consoleManager.getTerminal().writer().println("Координаты: " + musicBand.getCoordinates());
        }
        return true;
    }
}