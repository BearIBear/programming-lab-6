package server.commands;

import java.util.Arrays;


import models.MusicBand;
import models.MusicGenre;
import server.managers.CollectionManager;
import util.CommandResult;

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
    public CommandResult run(String[] args, MusicBand band) {
        String genre = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        String [] newArgs = {args[0], genre};

        CommandResult commandResult = checkArgAmount(newArgs);
        if (!commandResult.isContinueFlag()) {
            return commandResult;
        }

        collectionManager.getCollection().stream().filter(band1 -> band1.getGenre().compareTo(MusicGenre.valueOf(genre)) > 0)
         .forEach(band1 -> commandResult.addToMessage(band1.toString()));
        return commandResult;
    }
}