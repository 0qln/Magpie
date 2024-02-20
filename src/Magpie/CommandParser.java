import java.util.*;

public class CommandParser
{
    private IBoard board;
    
    public CommandParser(IBoard board)
    {
        this.board = board;
    }
    
    public Optional<ICommand> Parse(String input) {
        String[] args = input.split(" ");
        switch (args[0]) {
            case "uci":
                return Optional.of(new UciUciCommand());
            default: 
                // Ignore invalid input commands
                return Optional.empty();
        }
    }
}
