import java.util.HashMap;
import java.util.Map;

public class PieceType
{
    public static final int None = -1;    

    public static final int Pawn = 0;
    public static final int Knight = 1;
    public static final int Bishop = 2;
    public static final int Rook = 3;
    public static final int Queen = 4;
    public static final int King = 5;

    public static final Map<Character, Integer> Map = 
        new HashMap<>(7)
    {{
        put('.', None);
        put('p', Pawn);
		put('n', Knight);
		put('b', Bishop);
		put('r', Rook);
		put('q', Queen);
		put('k', King);
    }};
}
