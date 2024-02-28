package Engine;

public class PawnMoveGenerator extends MoveGenerator
{
    private static final long LEFT = 0x7F7F7F7F7F7F7F7FL;
    private static final long RIGHT = 0xFEFEFEFEFEFEFEFEL;
    // Indexed by color
    private static final long[] DOUBLE = new long[] { 0xFF00L, 0xFF000000000000L };

    @Override
    int generate(short[] list, int index, Board board, int color) {
        return color == Color.White 
            ? white(list, index, board) 
            : black(list, index, board);
    }

    private int white(short[] list, int index, Board board) {
        Utils.printBB(LEFT);
        Utils.printBB(RIGHT);
        long[] pawnsBB = new long[] { board.getBitboard(PieceType.Pawn, Color.White) };
        final long enemies = board.getCBitboard(Color.Black);
        Utils.printBB(pawnsBB[0]);
        long[] rightBB = new long[] { ((pawnsBB[0] & RIGHT) << 7) & enemies };
        long[] leftBB = new long[] { ((pawnsBB[0] & LEFT) << 9) & enemies };
        long[] singleBB = new long[] { pawnsBB[0] << 8 };
        long[] doubleBB = new long[] { (pawnsBB[0] & DOUBLE[Color.White]) << 16 };
        Utils.printBB(singleBB[0] | leftBB[0] | rightBB[0] | doubleBB[0]);

        while (true) {
            final int from = Utils.popLsb(pawnsBB);
            final int single = Utils.popLsb(singleBB);
            final int double_ = Utils.popLsb(doubleBB);
            final int left = Utils.popLsb(leftBB);
            final int right = Utils.popLsb(rightBB);

            list[index++] = Move.create(from, single, Move.QUIET_MOVE_FLAG);
            list[index++] = Move.create(from, left, Move.QUIET_MOVE_FLAG);
            list[index++] = Move.create(from, right, Move.QUIET_MOVE_FLAG);
            list[index++] = Move.create(from, double_, Move.DOUBLE_PAWN_PUSH_FLAG);

            if (pawnsBB[0] == 0) {
                break;
            }
        }
        
        return index;
    }

    private int black(short[] list, int index, Board board) {
        return index;
    }
}
