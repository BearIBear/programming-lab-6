package commands;

import managers.ConsoleManager;
import managers.CollectionManager;

/**
 * Команда для вывода первого элемента коллекции (head)
 *
 * @author Михаил
 */
public class Head extends Command {
    public Head(CollectionManager collectionManager, ConsoleManager consoleManager) {
        super("head", "вывести первый элемент коллекции", 0, collectionManager, consoleManager);
    }

    @Override
    public boolean run(String[] args) {
        if (!checkArgAmount(args)) {
            return true;
        }
        
        if (collectionManager.getCollection().size() == 0) {
            consoleManager.getTerminal().writer().println("\u001B[31m" + this.name + " : Коллекция пустая" + "\u001B[0m");
            return true;
        }

        consoleManager.getTerminal().writer().println(collectionManager.getCollection().peek());

        return true;
    }
}