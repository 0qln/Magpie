package Interface;

import java.util.*;

public class Main
{
    public static Scanner scanner = new Scanner(System.in);
    public static void main(String[] args)
    {
        Engine.IBoard board = new Engine.Board();
        CommandParser parser = new CommandParser();
        while (true) {
            String input = scanner.next();
            Optional<ICommandBuilder> commandBuilder = parser.parse(input);
            commandBuilder.ifPresent(
                // UCI:: The engine must always be able to process input from stdin, even while thinking.
                builder -> builder.buildForBoard(board).run()
            );
        }
    }
}
