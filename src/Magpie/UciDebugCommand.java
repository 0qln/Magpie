import java.util.*;

public class UciDebugCommand extends UciCommand
{
    private boolean _on;
    
    public UciDebugCommand(IBoard board, boolean on)
    {
        super(board);
        _on = on;
    }
    
    public static Optional<Boolean> parseValue(String value) {
        switch (value) {
            case "on": return Optional.of(Boolean.valueOf(true));
            case "off": return Optional.of(Boolean.valueOf(false));
            default: return Optional.empty();
        }
    }
}
