package Engine;

public class PawnMoveGenerator extends MoveGenerator
{
    private static final long LEFT = 0x7F7F7F7F7F7F7F7FL;
    private static final long RIGHT = 0x7E7E7E7E7E7E7E7EL;

    @Override
    int generate(short[] list, int index, Board board, int color) {
        return color == Color.White 
            ? white(list, index, board) 
            : black(list, index, board);
    }

    private int white(short[] list, int index, Board board) {
        long[] pawnsBB = new long[] { board.getBitboard(PieceType.Pawn, Color.White) };
        long[] rightBB = new long[] { (pawnsBB[0] & RIGHT) << 7 };
        long[] leftBB = new long[] { (pawnsBB[0] & LEFT) << 9 };
        long[] stepBB = new long[] { pawnsBB[0] << 8 };

        while (true) {
            final int from = Utils.popLsb(pawnsBB);
            final int step = Utils.popLsb(stepBB);
            final int left = Utils.popLsb(leftBB);
            final int right = Utils.popLsb(rightBB);

            list[index++] = Move.create(from, step, Move.QUIET_MOVE_FLAG);

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
