package Interface;

import java.util.*;
import java.util.logging.Logger;

import Misc.LoggerConfigurator;
import Engine.Bishop;
import Engine.Rook;
import Engine.Zobrist;

public final class Main {

    public static final boolean DEBUG = false;
    public static final boolean RELEASE = !DEBUG;

    static {
        // java doesnt execute the static initializers without
        // the contructor being called atleast once...
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
        new UciNewGameCommand();
        new TestCommand();

        Zobrist.initSeed(4662395907542479981L);
        
        Bishop.MoveGenerator.Initialize();
        Rook.MoveGenerator.Initialize();
    }

    private static boolean _quitFlag = false;
    private static Scanner _scanner;
    private static Logger _logger;

    public static void quit() {
        _quitFlag = true;
        _scanner.close();
    }

    public static void main(String[] args) throws Misc.Builder.FieldNotSetException {
        _logger = LoggerConfigurator.configureLogger(Main.class);
        _scanner = new Scanner(System.in);
        _scanner.useDelimiter("\n");
        Misc.ProgramState state = new Misc.ProgramState();

        while (_quitFlag == false) {
            String input = _scanner.next();
            _logger.info("Input: " + input);
            handleInput(input, state);
        }
    }

    private static final void handleInput(String input, Misc.ProgramState state) {
        CommandParser.parse(input).ifPresent(builder -> {
            _logger.info("Command builder: " + builder.getClass().getName());
            ICommand command = builder.buildForBoard(state);
            _logger.info("Command : " + command.getClass().getName());
            if (command.canRun()) {
                if (command.shouldSync())
                    command.runSync();
                else
                    // UCI:: The engine must always be able to process input from stdin, even while
                    // thinking.
                    command.runAsync();
            }
        });
    }
}
