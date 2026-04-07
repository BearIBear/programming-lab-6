package server.commands;


import server.managers.CollectionManager;

/**
 * Команда для завершения работы программы (без сохранения в файл)
 *
 * @author Михаил
 */
public class Exit extends Command {
    public Exit(CollectionManager collectionManager) {
        super("exit", "завершить программу (без сохранения в файл)", 0, collectionManager);
    }

    @Override
    public boolean run(String[] args) {
        if (!checkArgAmount(args)) {
            return true;
        }
        return false;
    }
}