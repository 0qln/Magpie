
package Interface;

import java.util.*;
import java.util.logging.*;
import Misc.Utils;
import Misc.LoggerConfigurator;
import Engine.PieceUtil;

public class CommandParser {
    public static final String EmptyCommand = "EmptyCommand";
    private Logger logger = LoggerConfigurator.configureLogger(CommandParser.class);

    private Optional<ICommandBuilder> parse(String[] processedInput) {
        String[] args = getArguments(processedInput);

        // UCI:: if the engine or the GUI receives an unknown command or token it should
        // just ignore it and try to
        // parse the rest of the string in this line.
        // Examples: "joho debug on\n" should switch the debug mode on given that joho
        // is not defined,
        // "debug joho on\n" will be undefined however.

        switch (getCommand(processedInput).orElse(EmptyCommand)) {
            case "uci":
                logger.info("Parsing UCI command.");
                return Optional.of(b -> new Interface.UCI.UciCommand(b));

            case "setoption":
                logger.info("Parsing Setoption command.");
                if (args.length <= 3) {
                    return Optional.empty();
                }
                String name = args[1];
                Interface.UCI.Option<?> option = Config.getOption(name).get();
                Optional<?> valueOpt = option.getValueParser().parse(Arrays.copyOfRange(args, 3, args.length));
                if (valueOpt.isPresent()) {
                    return Optional
                            .of(b -> new Interface.UCI.SetOptionCommand<Object>(b, name, valueOpt.get()));
                }
                return Optional.empty();

            case "isready":
                logger.info("Parsing Isready command.");
                return Optional.of(b -> new Interface.UCI.IsreadyCommand(b));

            case "debug":
                logger.info("Parsing Debug command.");
                if (args.length <= 0) {
                    return Optional.empty();
                }
                Optional<Boolean> value = Interface.UCI.DebugCommand.parseValue(args[0]);
                if (!value.isPresent()) {
                    return Optional.empty();
                }
                return Optional.of(b -> new Interface.UCI.DebugCommand(b, value.get()));

            case "quit":
                logger.info("Parsing Quit command.");
                return Optional.of(b -> new Interface.UCI.QuitCommand(b));

            case "position":
                logger.info("Parsing Position command.");
                if (args.length <= 0) {
                    return Optional.empty();
                }
                int movesidx = Misc.Utils.indexOf(args, e -> e.equals("moves"));
                String[] moves = movesidx == -1 ? new String[0] : Arrays.copyOfRange(args, movesidx + 1, args.length);
                switch (args[0]) {
                    case "startposition":
                    case "startpos":
                        return Optional.of(b -> new Interface.UCI.StartposCommand(b, moves));
                    case "fen":
                        String[] fen = Arrays.copyOfRange(args, 1, movesidx == -1 ? args.length : movesidx);
                        return Optional.of(b -> new Interface.UCI.FenCommand(b, moves, fen));
                    default:
                        return Optional.empty();
                }

            case "print":
                logger.info("Parsing Print command.");
                return Optional.of(b -> new Interface.Custom.PrintCommand(b));

            case "piece":
                logger.info("Parsing Piece command.");
                if (args.length <= 1) {
                    return Optional.empty();
                }
                int square = Utils.toSquareIndex(args[1]);
                logger.info("Square: " + square);
                switch (args[0]) {
                    case "remove":
                        return Optional.of(b -> new Interface.Custom.PieceRemoveCommand(b, square));
                    case "get":
                        return Optional.of(b -> new Interface.Custom.PieceGetCommand(b, square));
                    case "add":
                        if (args.length <= 2) {
                            return Optional.empty();
                        }
                        int piece = PieceUtil.fromChar(args[2].charAt(0));
                        logger.info("Piece: " + piece);
                        return Optional.of(b -> new Interface.Custom.PieceAddCommand(b, square, piece));
                    default:
                        return Optional.empty();
                }

            case "perft":
                logger.info("Parsing Perft command.");
                return Optional.of(b -> new Interface.Custom.PerftCommand(b,
                        args.length == 1
                                ? Optional.of(Integer.parseInt(args[0]))
                                : Optional.empty()));

            case "info":
                logger.info("Parsing Info command.");

                if (args.length <= 0)
                    return Optional.empty();

                switch (args[0]) {
                    case "check":
                        logger.info("Parsing Check Info command.");
                        return Optional.of(b -> new Interface.Custom.CheckInfoCommand(b));
                    case "castling":
                        logger.info("Parsing Castling Info command.");
                        return Optional.of(b -> new Interface.Custom.CastlingInfoCommand(b));
                    default:
                        return Optional.empty();
                }

            case "go":
                logger.info("Parsing Go command.");

                if (args.length <= 0)
                    return Optional.empty();

                switch (args[0]) {
                    case "perft":
                        return Optional.of(b -> new Interface.Custom.PerftCommand(b, args.length == 2
                                ? Optional.of(Integer.parseInt(args[1]))
                                : Optional.empty()));

                    default:
                        return Optional.empty();
                }

            default:
            case EmptyCommand:
                logger.warning("Empty or unknown command.");
                return Optional.empty();
        }
    }

    public Optional<ICommandBuilder> parse(String input) {
        logger.info("Parsing input: " + input);

        String[] processedInput = getTokens(input);
        Optional<ICommandBuilder> result = parse(processedInput);

        // UCI:: If the engine or the GUI receives an unknown command or token it should
        // just ignore it and try to parse the rest of the string in this line.
        while (processedInput.length > 1 && !result.isPresent()) {
            // Remove the first element of the array.
            processedInput = Arrays.copyOfRange(processedInput, 1, processedInput.length);
            result = parse(processedInput);
        }

        return result;
    }

    private String[] getTokens(String input) {
        logger.info("Getting tokens from input: " + input);
        // UCI:: arbitrary white space between tokens is allowed
        // Example: "debug on\n" and " debug on \n" and "\t debug \t \t\ton\t \n"
        // all set the debug mode of the engine on.
        input = input.replaceAll("\\s+", " ");

        // Get tokens
        String[] result = input.split(" ");

        logger.info("Extracted tokens: " + String.join(", ", result));

        return result;
    }

    private Optional<String> getCommand(String[] processedInput) {
        logger.info("Getting command from processed input.");
        return (processedInput.length > 0) ? Optional.of(processedInput[0]) : Optional.empty();
    }

    private String[] getArguments(String[] processedInput) {
        logger.info("Getting arguments from processed input.");
        String[] result = Arrays.copyOfRange(processedInput, 1, processedInput.length);
        logger.info("Extracted args: " + String.join(", ", result));
        return result;
    }
}
