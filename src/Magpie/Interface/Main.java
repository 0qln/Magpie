package Interface;

import java.util.*;
import java.util.logging.Logger;

import Misc.LoggerConfigurator;


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
        new StopCommand();
    }

    public static void quit() {
        _quitFlag = true;
        
    }
    private static boolean _quitFlag = false;
    private static Scanner scanner = new Scanner(System.in);
    private static Logger logger = LoggerConfigurator.configureLogger(Main.class);

    public static void main(String[] args) throws Misc.Builder.FieldNotSetException {
        scanner.useDelimiter("\n");
        Misc.ProgramState state = new Misc.ProgramState();
        state.board.set(new Engine.Board.Builder().build());

        // Set up startposition
        handleInput("position startpos", state);

        while (true) {
            String input = scanner.next();
            logger.info("Input: " + input);
            handleInput(input, state);
        }
    }

    private static final void handleInput(String input, Misc.ProgramState state) {
        CommandParser.parse(input).ifPresent(
                // UCI:: The engine must always be able to process input from stdin, even while
                // thinking.
                builder -> {
                    logger.info("Command builder: " + builder.getClass().getName());
                    ICommand command = builder.buildForBoard(state);
                    logger.info("Command : " + command.getClass().getName());
                    if (command.canRun()) {
                        command.runAsync();
                    }
                });
    }
}
