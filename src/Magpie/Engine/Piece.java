package Engine;

public abstract class Piece {

    public abstract MoveGenerator getGenerator();

    public static abstract class MoveGenerator {
        // Returns index of next empty list slot.
        abstract int generate(short[] list, int index, Board board, int color, boolean capturesOnly);

        // Returns index of next empty list slot.
        abstract int resolves(short[] list, int index, Board board, int color, boolean capturesOnly);

        public abstract long attacks(int square, int color);
        public abstract long attacks(int square, long occupied, int color);
    }

    public static Piece fromID(int id) {
        switch (PieceUtil.getType(id)) {
            case PieceType.Pawn: return new Pawn();
            case PieceType.Knight: return new Knight();
            case PieceType.Bishop: return new Bishop();
            case PieceType.Rook: return new Rook();
            case PieceType.Queen: return new Queen();
            case PieceType.King: return new King();
            default: return null;
        }
    }
}
