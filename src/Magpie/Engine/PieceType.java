package Engine;

import java.util.HashMap;
import java.util.Map;

public abstract class PieceType {

    public abstract MoveGenerator getGenerator();

    public static final Map<Character, Integer> CMap = new HashMap<Character, Integer>(7)
    {{
        put('.', None.ID_Type);
        put('p', Pawn.ID_Type);
        put('n', Knight.ID_Type);
        put('b', Bishop.ID_Type);
        put('r', Rook.ID_Type);
        put('q', Queen.ID_Type);
        put('k', King.ID_Type);
    }};

    public static final Map<Integer, Character> PMap = new HashMap<Integer, Character>(7)
    {{
        put(None.ID_Type, '.');
        put(Pawn.ID_Type, 'p');
        put(Knight.ID_Type, 'n');
        put(Bishop.ID_Type, 'b');
        put(Rook.ID_Type, 'r');
        put(Queen.ID_Type, 'q');
        put(King.ID_Type, 'k');
    }};
    
    public static abstract class MoveGenerator {
        // Returns index of next empty list slot.
        abstract int generate(short[] list, int index, Board board, int color, boolean capturesOnly);

        // Returns index of next empty list slot.
        abstract int resolves(short[] list, int index, Board board, int color, boolean capturesOnly);

        public abstract long attacks(int square, int color);
        public abstract long attacks(int square, long occupied, int color);
    }

    public static PieceType fromID(int id) {
        switch (Piece.getType(id)) {
            case Pawn.ID_Type: return new Pawn();
            case Knight.ID_Type: return new Knight();
            case Bishop.ID_Type: return new Bishop();
            case Rook.ID_Type: return new Rook();
            case Queen.ID_Type: return new Queen();
            case King.ID_Type: return new King();
            default: return null;
        }
    }
}
