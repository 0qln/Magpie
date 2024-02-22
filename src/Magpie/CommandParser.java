import java.util.*;

public class CommandParser
{
    public static final String EmptyCommand = "EmptyCommand";
    public static final Optional<ICommandBuilder> UnexpectedToken = Optional.empty();
    public static final Optional<ICommandBuilder> TokenUnderflow = Optional.empty();
    
    
    public Optional<ICommandBuilder> parse(String input) {
        String[] processedInput = getTokens(input);
        String[] args = getArguments(processedInput);
        
        // UCI:: if the engine or the GUI receives an unknown command or token it should just ignore it and try to
              // parse the rest of the string in this line.
              // Examples: "joho debug on\n" should switch the debug mode on given that joho is not defined,
              //           "debug joho on\n" will be undefined however.
                            
        switch (getCommand(processedInput).orElse(EmptyCommand)) {
            case "uci":
                return Optional.of((board) -> new UciUciCommand(board));
                
            case "debug": 
                if (args.length <= 0) {
                    return TokenUnderflow;
                }                    
                Optional<Boolean> value = UciDebugCommand.parseValue(args[0]);
                if (!value.isPresent()) {
                    return UnexpectedToken;
                }                    
                return Optional.of((board) -> new UciDebugCommand(board, value.get()));
            
            case "quit": 
                return Optional.of((board) -> new UciQuitCommand(board));
                
            case "position": 
                if (args.length <= 0) {
                    return TokenUnderflow;
                }
                if (args[0] == "startpos") {
                    return Optional.of((board) ->  UciStartposCommand(board, ));
                }
                else if (args[0] == "fen") {
                    return Optional.of(board -> UciFenCommand(board, ))
                }
                return Optional.of((board) -> new UciPositionCommand(board));
                
            default: 
            case EmptyCommand:    
                return Optional.empty();
        }
    }
    
    private String[] getTokens(String input) {
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
        return (processedInput.length > 0) ? Optional.of(processedInput[0]) : Optional.empty();
    }
    
    private String[] getArguments(String[] processedInput) {
        return Arrays.copyOfRange(processedInput, 1, processedInput.length);
    }
}
