package server.commands;


import java.util.Map;

import models.MusicBand;
import server.managers.CollectionManager;
import util.CommandResult;

/**
 * Команда для вывода справки по доступным командам
 *
 * @author Михаил
 */
public class Help extends Command {
    public Help(CollectionManager collectionManager) {
        super("help", "вывести справку по доступным командам", 0, collectionManager);
    }

    @Override
    public CommandResult run(String[] args, MusicBand band) {
        CommandResult commandResult = checkArgAmount(args);
        if (!commandResult.isContinueFlag()) {
            return commandResult;
        }

        int max_length = 0;
        int padding = 5;
        Map<String, Command>  commandsList = commandManager.getCommandsList();
        for (String name : commandsList.keySet()) {
            max_length = Math.max(max_length, name.length());
        }
        for (String name : commandsList.keySet()) {
            if (!name.equals("save")) {
                commandResult.addToMessage(name + " ".repeat(max_length + padding - name.length()) + commandsList.get(name).getDesc());
            }
        }
        commandResult.setContinueFlag(true);
        return commandResult;
    }
}
