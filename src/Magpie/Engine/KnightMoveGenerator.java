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

        long[] knights = { board.getBitboard(PieceType.Knight, color) };
        long k = knights[0];

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
        long sao = Utils.lshift(board.getCBitboard(color), -loff);
        long[] fromBB = { knights & ~sao };
       
        while (fromBB[0] != 0) {
            final int from = Utils.popLsb(fromBB);
            final int to = from + loff;
            list[index++] = Move.create(from, to, board.getPiece(to) == Piece.None[0] 
                ? Move.QUIET_MOVE_FLAG 
                : Move.CAPTURE_FLAG);
        }

        return index;
    }


/* ISSUE:

position fen 7k/4P1P1/3P3P/5N2/3P3P/4P1P1/8/K7 w - - 0 1
perft
f5h4: 1
f5d4: 1
Node count: 2
print
8  . . . . . . . k
7  . . . . P . P .
6  . . . P . . . P
5  . . . . . N . .
4  . . . P . . . P
3  . . . . P . P .
2  . . . . . . . .
1  K . . . . . . .
   a b c d e f g h 
*/

}
