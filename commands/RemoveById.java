package commands;

import managers.ConsoleManager;

import managers.CollectionManager;
import models.MusicBand;

/**
 * Команда для удаления элемента из коллекции по его id
 *
 * @author Михаил
 */
public class RemoveById extends Command {
    public RemoveById(CollectionManager collectionManager, ConsoleManager consoleManager) {
        super("remove_by_id", "удалить элемент из коллекции по его id", 1, collectionManager, consoleManager);
    }

    @Override
    public boolean run(String[] args) {
        args = Command.RemoveEmptyElements(args);
        if (!checkArgAmount(args)) {
            return true;
        }
        try {
            long id = Long.parseLong(args[1]);
            if (collectionManager.removeElement(id)) {
                consoleManager.getTerminal().writer().println("Элемент с id = " + id + " успешно удалён");
                MusicBand.addVacantId(id);
            } else {
                consoleManager.getTerminal().writer().println("\u001B[31m" + this.name + " : Элемент с id = " + id + " не найден" + "\u001B[0m");
            }
        } catch (NumberFormatException e) {
            consoleManager.getTerminal().writer().println("\u001B[31m" + this.name + "remove_by_id : Позиционный параметр id принимает только значения формата long" + "\u001B[0m");
        }
        return true;
    }
}