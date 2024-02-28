package Engine;

public class KnightMoveGenerator extends MoveGenerator {

    private static final long MASK_MID       = 0x00003C3C3C3C0000L;
    private static final long MASK_WEST      = 0x7F7F7F7F7F7F7F7FL;
    private static final long MASK_WEST_WEST = 0x3F3F3F3F3F3F3F3FL;
    private static final long MASK_EAST      = 0xFEFEFEFEFEFEFEFEL;
    private static final long MASK_EAST_EAST = 0xFCFCFCFCFCFCFCFCL;
    private static final long MASK_SOUT      = 0xFFFFFFFFFFFFFF00L;
    private static final long MASK_SOUT_SOUT = 0xFFFFFFFFFFFF0000L;
    private static final long MASK_NORT      = 0x00FFFFFFFFFFFFFFL;
    private static final long MASK_NORT_NORT = 0x0000FFFFFFFFFFFFL;


    @Override
    int generate(short[] list, int index, Board board, int color) {

        long k = board.getBitboard(PieceType.Knight, color);

        index = addMoves(list, index, board, color, k & MASK_WEST_WEST & MASK_NORT, +10);
        index = addMoves(list, index, board, color, k & MASK_WEST_WEST & MASK_SOUT, -06);
        index = addMoves(list, index, board, color, k & MASK_EAST_EAST & MASK_SOUT, -10);
        index = addMoves(list, index, board, color, k & MASK_EAST_EAST & MASK_NORT, +06);
        index = addMoves(list, index, board, color, k & MASK_WEST & MASK_NORT_NORT, +17);
        index = addMoves(list, index, board, color, k & MASK_WEST & MASK_SOUT_SOUT, -15);
        index = addMoves(list, index, board, color, k & MASK_EAST & MASK_SOUT_SOUT, -17);
        index = addMoves(list, index, board, color, k & MASK_EAST & MASK_NORT_NORT, +15);

        return index;
    }

    private int addMoves(short[] list, int index, Board board, int color, long knights, int loff) {
        // Remove all knights whose dest square is occupied by an ally.
        final long sao = loff > 0 ? board.getCBitboard(color) >>> loff : board.getCBitboard(color) << -loff;
        final long[] fromBB = { knights & ~sao };

        Utils.printBB(sao);
        Utils.printBB(fromBB);
       
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
