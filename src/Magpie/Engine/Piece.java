package Engine;

public final class Piece
{
    public static int getType(int piece) {
        return piece >>> 1;
    }

    public static int getColor(int piece) {
        return piece & 0x1;
    }

    public static int create(int type, int color) {
        return type << 1 | color;
    }

    public static char toChar(int piece) {
        char c = PieceType.PMap.get(getType(piece));
        if (getColor(piece) == Color.White) {
            c = Character.toUpperCase(c);
        }
        return c;
    }

    public static int fromChar(char piece) {
        int color = Character.isUpperCase(piece) ? Color.White : Color.Black;
        int type = PieceType.CMap.get(Character.toLowerCase(piece));
        return (type << 1) | color;
    }
}