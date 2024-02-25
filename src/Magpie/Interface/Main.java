package Interface;

import java.util.*;
import java.util.logging.Logger;

import Misc.LoggerConfigurator;

public final class Main
{
    public static Scanner scanner = new Scanner(System.in);
    private static Logger logger = LoggerConfigurator.configureLogger(Main.class);

    public static void main(String[] args)
    {
        scanner.useDelimiter("\n");
        Engine.IBoard board = new Engine.Board();
        CommandParser parser = new CommandParser();
        while (true) {
            String input = scanner.next();
            logger.info("Input: " + input);
            Optional<ICommandBuilder> commandBuilder = parser.parse(input);
            commandBuilder.ifPresent(
                // UCI:: The engine must always be able to process input from stdin, even while thinking.
                builder -> {
                    logger.info("Command builder: " + builder.getClass().getName());
                    ICommand command = builder.buildForBoard(board);
                    logger.info("Command : " + command.getClass().getName());
                    command.run();
                }
            );
        }
    }
}
