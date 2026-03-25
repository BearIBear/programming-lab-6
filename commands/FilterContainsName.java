package commands;

import managers.ConsoleManager;

import java.util.Arrays;

import managers.CollectionManager;
import models.MusicBand;

/**
 * Команда для вывода элементов, значение поля name которых содержит заданную подстроку
 *
 * @author Михаил
 */
public class FilterContainsName extends Command {
    public FilterContainsName(CollectionManager collectionManager, ConsoleManager consoleManager) {
        super("filter_contains_name", "вывести элементы, значение поля name которых содержит заданную подстроку", 1, collectionManager, consoleManager);
    }

    @Override
    public boolean run(String[] args) {
        String description = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        String [] newArgs = {args[0], description};
        if (!checkArgAmount(newArgs)) {
            return true;
        }

        for (MusicBand band : collectionManager.getCollection()) {
            if (band.getName().contains(newArgs[1])) {
                consoleManager.getTerminal().writer().println(band);
            }
        }
        return true;
    }
}