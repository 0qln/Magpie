package Engine;

public class KnightMoveGenerator extends MoveGenerator {

    @Override
    int generate(short[] list, int index, Board board, int color) {

        long knights = board.getBitboard(PieceType.Knight, color);

        index = addMoves(list, index, board, color, knights & Masks.NoEaEa, CompassRose.NoEaEa);
        index = addMoves(list, index, board, color, knights & Masks.SoEaEa, CompassRose.SoEaEa);
        index = addMoves(list, index, board, color, knights & Masks.SoWeWe, CompassRose.SoWeWe);
        index = addMoves(list, index, board, color, knights & Masks.NoWeWe, CompassRose.NoWeWe);
        index = addMoves(list, index, board, color, knights & Masks.NoNoEa, CompassRose.NoNoEa);
        index = addMoves(list, index, board, color, knights & Masks.SoSoEa, CompassRose.SoSoEa);
        index = addMoves(list, index, board, color, knights & Masks.SoSoWe, CompassRose.SoSoWe);
        index = addMoves(list, index, board, color, knights & Masks.NoNoWe, CompassRose.NoNoWe);

        return index;
    }

    private int addMoves(short[] list, int index, Board board, int color, long knights, int loff) {
        // Remove all knights whose dest square is occupied by an ally.
        final long sao = Utils.rshift(board.getCBitboard(color), loff);
        final long[] fromBB = { knights & ~sao };

        while (fromBB[0] != 0) {
            final int from = Utils.popLsb(fromBB);
            final int to = from + loff;
            list[index++] = Move.create(from, to, board.getPiece(to) == Piece.None[0]
                    ? Move.QUIET_MOVE_FLAG
                    : Move.CAPTURE_FLAG);
        }

        return index;
    }
}
