package commands;

import managers.ConsoleManager;

import java.util.Arrays;

import managers.CollectionManager;
import models.MusicBand;

/**
 * Команда для вывода количества элементов, значение поля description которых меньше заданного
 *
 * @author Михаил
 */
public class CountLessThanDescription extends Command {
    public CountLessThanDescription(CollectionManager collectionManager, ConsoleManager consoleManager) {
        super("count_less_than_description", "вывести количество элементов, значение поля description которых меньше заданного", 1, collectionManager, consoleManager);
    }

    @Override
    public boolean run(String[] args) {
        String description = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        String [] newArgs = {args[0], description};
        if (!checkArgAmount(newArgs)) {
            return true;
        }

        int count = 0; 
        for (MusicBand band : collectionManager.getCollection()) {
            if (band.getDescription().compareTo(description) < 0) {
                count += 1;
            }
        }
        consoleManager.getTerminal().writer().println(count);
        return true;
    }
}