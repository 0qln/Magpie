
package Interface;

import java.util.*;
import java.util.logging.*;
import Misc.LoggerConfigurator;

public class CommandParser {
    private static Logger logger = LoggerConfigurator.configureLogger(CommandParser.class);

    public static Optional<ICommandBuilder> parse(String input) {
        logger.info("Parsing input: " + input);

        String[] processedInput = getTokens(input);
        if (processedInput.length == 0) {
            return Optional.empty();
        }

        Interface.Command.Signature<?> commandIter = null;

        // UCI:: If the engine or the GUI receives an unknown command or token it should
        // just ignore it and try to parse the rest of the string in this line.
        do {
            // Check if the first token is a valid command.
            for (Interface.Command.Signature<?> uciCommand : Interface.Command.Signature.enumerate()) {
                if (uciCommand.protocolName.equals(processedInput[0])) {
                    commandIter = uciCommand;
                    break;
                }
            }
            if (commandIter != null)
                break;

            // Remove the first element of the array.
            processedInput = Arrays.copyOfRange(processedInput, 1, processedInput.length);
        } while (processedInput.length > 1);

        // Unknown command
        if (commandIter == null) {
            return Optional.empty();
        }

        final Interface.Command.Signature<?> command = commandIter;
        final String[] args = getArguments(processedInput);

        logger.info("Command: " + command.protocolName);

        return Optional.of(state -> {
            return command.commandBuilder
                    .state(state)
                    .args(args)
                    .build(true);
        });
    }

    private static String[] getTokens(String input) {
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

    private static String[] getArguments(String[] processedInput) {
        if (processedInput.length <= 1)
            return new String[0];

        logger.info("Getting arguments from processed input.");
        String[] result = Arrays.copyOfRange(processedInput, 1, processedInput.length);
        logger.info("Extracted args: " + String.join(", ", result));
        return result;
    }
}
