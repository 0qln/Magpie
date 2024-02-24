
package Interface;

import java.util.*;
import java.util.logging.*;
import Misc.Utils;
import Misc.LoggerConfigurator;
import Engine.Piece;

public class CommandParser
{
    public static final String EmptyCommand = "EmptyCommand";
    public static final Optional<ICommandBuilder> UnexpectedToken = Optional.empty();
    public static final Optional<ICommandBuilder> TokenUnderflow = Optional.empty();
    private Logger logger = LoggerConfigurator.configureLogger(CommandParser.class);
    

    @SuppressWarnings("unchecked")
    public Optional<ICommandBuilder> parse(String input) {
        logger.info("Parsing input: " + input);
        
        String[] processedInput = getTokens(input);
        String[] args = getArguments(processedInput);
        
        // UCI:: if the engine or the GUI receives an unknown command or token it should just ignore it and try to
              // parse the rest of the string in this line.
              // Examples: "joho debug on\n" should switch the debug mode on given that joho is not defined,
              //           "debug joho on\n" will be undefined however.
                            
        switch (getCommand(processedInput).orElse(EmptyCommand)) {
            case "uci":
                logger.info("Parsing UCI command.");
                return Optional.of(board -> new Interface.UCI.UciCommand(board));

            case "setoption":
                logger.info("Parsing Setoption command.");
                if (args.length <= 3) {
                    return TokenUnderflow;
                }
                String name = args[1];
                Interface.UCI.Option option = Config.getOption(name).get();               
                Optional valueOpt = option.getValueParser().parse(Arrays.copyOfRange(args, 3, args.length));
                if (valueOpt.isPresent()) {
                    return Optional.of(board -> new Interface.UCI.SetOptionCommand(board, name, valueOpt.get()));
                }
                return Optional.empty(); 

            case "isready":
                logger.info("Parsing Isready command.");
                return Optional.of(board -> new Interface.UCI.IsreadyCommand(board));
                
            case "debug": 
                logger.info("Parsing Debug command.");
                if (args.length <= 0) {
                    return TokenUnderflow;
                }                    
                Optional<Boolean> value = Interface.UCI.DebugCommand.parseValue(args[0]);
                if (!value.isPresent()) {
                    return UnexpectedToken;
                }                    
                return Optional.of(board -> new Interface.UCI.DebugCommand(board, value.get()));
            
            case "quit": 
                logger.info("Parsing Quit command.");
                return Optional.of(board -> new Interface.UCI.QuitCommand(board));
                
            case "position": 
                logger.info("Parsing Position command.");
                if (args.length <= 0) {
                    return TokenUnderflow;
                }
                int movesidx = Misc.Utils.indexOf(args, e -> e.equals("moves"));
                String[] moves = movesidx == -1 ? new String[0] : Arrays.copyOfRange(args, movesidx + 1, args.length);
                if (args[0] == "startpos") {
                    return Optional.of(board ->  new Interface.UCI.StartposCommand(board, moves));
                }
                else if (args[0] == "fen") {
                    String[] fen = Arrays.copyOfRange(args, 1, movesidx == -1 ? args.length : movesidx);
                    return Optional.of(board -> new Interface.UCI.FenCommand(board, moves, fen));
                }
                else return TokenUnderflow;
                
            case "print":
                logger.info("Parsing Print command.");
                return Optional.of(board -> new Interface.Custom.PrintCommand(board));

            case "piece":
                logger.info("Parsing Piece command.");
                if (args.length <= 1) {
                    return TokenUnderflow;
                }
                int square = Utils.toSquareIndex(args[1]);
                switch (args[0]) {
                    case "remove": return Optional.of(board -> new Interface.Custom.PieceRemoveCommand(board, square));
                    case "get": return Optional.of(board -> new Interface.Custom.PieceGetCommand(board, square));
                    case "add": 
                        if (args.length <= 2) return TokenUnderflow;
                        return Optional.of(board -> new Interface.Custom.PieceAddCommand(board, square, Piece.fromChar(args[2].charAt(0))));
                    default: return UnexpectedToken;
                }

            default: 
            case EmptyCommand:    
                logger.warning("Empty or unknown command.");
                return Optional.empty();
        }
    }
    
    private String[] getTokens(String input) {
        logger.info("Getting tokens from input: " + input);
        // UCI:: arbitrary white space between tokens is allowed
        //       Example: "debug on\n" and  "   debug     on  \n" and "\t  debug \t  \t\ton\t  \n"
        //       all set the debug mode of the engine on.
        input = input.replaceAll("\\s+", " ");
        
        // Get tokens
        String[] result = input.split(" ");
        
        // UCI:: If the engine or the GUI receives an unknown command or token it should 
        //       just ignore it and try to parse the rest of the string in this line.
        while (result.length > 1 && !parse(result[0]).isPresent()) {
            // Remove the first element of the array.
            result = Arrays.copyOfRange(result, 1, result.length);
        }
        
        return result;
    }
    
    private Optional<String> getCommand(String[] processedInput) {
        logger.info("Getting command from processed input.");
        return (processedInput.length > 0) ? Optional.of(processedInput[0]) : Optional.empty();
    }
    
    private String[] getArguments(String[] processedInput) {
        logger.info("Getting arguments from processed input.");
        return Arrays.copyOfRange(processedInput, 1, processedInput.length);
    }
}
