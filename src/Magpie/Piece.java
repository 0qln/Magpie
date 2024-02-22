public final class Piece
{
    public static final int None = -1;

    public static final int BlackPawn   = PieceType.Pawn   << 1 | Color.Black;
    public static final int BlackKnight = PieceType.Knight << 1 | Color.Black;
    public static final int BlackBishop = PieceType.Bishop << 1 | Color.Black;
    public static final int BlackRook   = PieceType.Rook   << 1 | Color.Black;
    public static final int BlackQueen  = PieceType.Queen  << 1 | Color.Black;
    public static final int BlackKing   = PieceType.King   << 1 | Color.Black;

    public static final int WhitePawn   = PieceType.Pawn   << 1 | Color.White;
    public static final int WhiteKnight = PieceType.Knight << 1 | Color.White;
    public static final int WhiteBishop = PieceType.Bishop << 1 | Color.White;
    public static final int WhiteRook   = PieceType.Rook   << 1 | Color.White;
    public static final int WhiteQueen  = PieceType.Queen  << 1 | Color.White;
    public static final int WhiteKing   = PieceType.King   << 1 | Color.White;

    public static int parse(char charNotation) {
        int color = Character.isUpperCase(charNotation) ? Color.Black : Color.White;
        int type = PieceType.Map.get(Character.toLowerCase(charNotation));
        return type << 1 | color;
    }
}