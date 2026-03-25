package commands;

import managers.ConsoleManager;

import java.util.Arrays;
import java.util.function.Predicate;

import managers.CollectionManager;
import managers.CommandManager;

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
    protected ConsoleManager consoleManager;

    public Command(String name, String desc, int argsAmount, CollectionManager collectionManager, ConsoleManager consoleManager) {
        this.name = name;
        this.desc = desc;
        this.argsAmount = argsAmount + 1;
        this.collectionManager = collectionManager;
        this.consoleManager = consoleManager;
    }

    public abstract boolean run(String[] args);
    
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