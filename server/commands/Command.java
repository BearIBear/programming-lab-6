package server.commands;

import java.util.Arrays;
import java.util.function.Predicate;


import server.managers.CollectionManager;
import server.managers.CommandManager;

/**
 * Абстрактный базовый класс для всех команд
 *
 * @author Михаил
 */
public abstract class Command {
    protected final String name;
    private final String desc;
    private final int argsAmount;
    protected CommandManager commandManager;
    protected CollectionManager collectionManager;

    public Command(String name, String desc, int argsAmount, CollectionManager collectionManager) {
        this.name = name;
        this.desc = desc;
        this.argsAmount = argsAmount + 1;
        this.collectionManager = collectionManager;
    }

    public abstract boolean run(String[] args); // TODO: Добавить ещё в аргументы сюда MusicBand band, который может быть null
    // TODO: boolean заменить на кастомный класс, содержащий: stopFlag (состояние работы программы), message (сообщение, привязанное к состоянию)
    
    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }
    
    public CommandManager getCommandManager() {
        return commandManager;
    }

    public CollectionManager getCollectionManager() {
        return collectionManager;
    }

    public void setCommandManager(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    public boolean checkArgAmount(String[] args) {
        if (args.length < this.argsAmount) {
            consoleManager.getTerminal().writer().println("\u001B[31m" + this.name + " : Недостаточно параметров" + "\u001B[0m");
            return false;
        }
        if (args.length > this.argsAmount) {
            consoleManager.getTerminal().writer().println("\u001B[31m" + this.name + " : Слишком много параметров" + "\u001B[0m");
            return false;
        }
        return true;
    }

    public static String[] RemoveEmptyElements(String[] input) {
        return Arrays.stream(input).filter(Predicate.not(String::isBlank)).toArray(String[]::new);
    }
}