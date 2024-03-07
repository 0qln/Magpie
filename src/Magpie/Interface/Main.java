package Interface;

import java.util.*;
import java.util.logging.Logger;

import Misc.LoggerConfigurator;
import Misc.Ptr;

public final class Main {
    static {
        // Fuck java
        new UciCommand();
        new IsreadyCommand();
        new PositionCommand();
        new GoCommand();
        new InfoCommand();
        new SetOptionCommand();
        new PieceCommand();
        new PrintCommand();
        new QuitCommand();
    }

    public static Scanner scanner = new Scanner(System.in);
    private static Logger logger = LoggerConfigurator.configureLogger(Main.class);

    public static void main(String[] args) throws Misc.Builder.FieldNotSetException {
        scanner.useDelimiter("\n");
        Ptr<Engine.IBoard> board = Ptr.to(new Engine.Board.Builder().build());

        // Set up startposition
        handleInput("position startpos", board);

        while (true) {
            String input = scanner.next();
            logger.info("Input: " + input);
            handleInput(input, board);
        }
    }

    private static final void handleInput(String input, Ptr<Engine.IBoard> board) {
        CommandParser.parse(input).ifPresent(
                // UCI:: The engine must always be able to process input from stdin, even while
                // thinking.
                builder -> {
                    logger.info("Command builder: " + builder.getClass().getName());
                    ICommand command = builder.buildForBoard(board);
                    logger.info("Command : " + command.getClass().getName());
                    if (command.canRun()) {
                        command.run();
                    }
                });
    }
}
