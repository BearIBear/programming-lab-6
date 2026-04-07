package server.commands;


import server.managers.CollectionManager;

/**
 * Команда для вывода информации о коллекции (тип, количество элементов, дата инициализации)
 *
 * @author Михаил
 */
public class Info extends Command {
    public Info(CollectionManager collectionManager) {
        super("info", "вывести информацию о коллекции", 0, collectionManager);
    }

    @Override
    public boolean run(String[] args) {
        if (!checkArgAmount(args)) {
            return true;
        }
        consoleManager.getTerminal().writer().println("Тип коллекции: PriorityQueue");
        consoleManager.getTerminal().writer().println("Количество элементов: " + collectionManager.getCollection().size());
        consoleManager.getTerminal().writer().println("Дата инициализации: " + collectionManager.getInitTime());
        return true;
    }
}