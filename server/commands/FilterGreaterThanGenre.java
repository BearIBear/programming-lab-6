package server.commands;

import java.util.Arrays;


import models.MusicBand;
import server.managers.CollectionManager;

/**
 * Команда для вывода элементов, значение поля genre которых больше заданного
 *
 * @author Михаил
 */
public class FilterGreaterThanGenre extends Command {
    public FilterGreaterThanGenre(CollectionManager collectionManager) {
        super("filter_greater_than_genre", "вывести элементы, значение поля genre которых больше заданного", 1, collectionManager);
    }

    @Override
    public boolean run(String[] args) {
        String genre = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        String [] newArgs = {args[0], genre};
        if (!checkArgAmount(newArgs)) {
            return true;
        }

        for (MusicBand band : collectionManager.getCollection()) {
            if (band.getGenre().toString().compareTo(genre) > 0) {
                consoleManager.getTerminal().writer().println(band);
            }
        }
        return true;
    }
}