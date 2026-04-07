package server.commands;


import server.managers.CollectionManager;

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
    public boolean run(String[] args) {
        if (!checkArgAmount(args)) {
            return true;
        }
        collectionManager.addElement(consoleManager.askMusicBand());
        consoleManager.getTerminal().writer().println("Банда добавлена успешно");
        return true;
    }
}