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
        final long PAWNS = board.getBitboard(PieceType.Pawn, Color.White);
        final long enemies = board.getCBitboard(Color.Black);
        final long pieces = enemies | board.getCBitboard(Color.White);
        long[] ptr = new long[1];
        long[] bb = new long[1];
        
        // Single step
        bb[0] = PAWNS;
        Utils.printBB(pieces);
        ptr[0] = (PAWNS << 8);
        ptr[0] ^= ptr[0] & pieces; // Exclude occupied squares 
        while (ptr[0] != 0) {
            final int from = Utils.popLsb(bb);
            final int to = Utils.popLsb(ptr);
            list[index++] = Move.create(from, to, Move.QUIET_MOVE_FLAG);
        }

        // Double step
        bb[0] = PAWNS;
        ptr[0] = ((PAWNS & DOUBLE[Color.White]) << 16);
        ptr[0] ^= ptr[0] & pieces; 
        ptr[0] ^= ((ptr[0] >> 8) & pieces) << 8;
        while (ptr[0] != 0) {
            final int from = Utils.popLsb(bb);
            final int to = Utils.popLsb(ptr);
            list[index++] = Move.create(from, to, Move.DOUBLE_PAWN_PUSH_FLAG);
        }

        // Capture right
        bb[0] = PAWNS;
        ptr[0] = ((PAWNS & RIGHT) << 7) & enemies;
        while (ptr[0] != 0) {
            final int from = Utils.popLsb(bb);
            final int to = Utils.popLsb(ptr);
            list[index++] = Move.create(from, to, Move.CAPTURE_FLAG);
        }
 
        // Capture left
        bb[0] = PAWNS;
        ptr[0] = ((PAWNS & LEFT) << 9) & enemies;
        while (ptr[0] != 0) {
            final int from = Utils.popLsb(bb);
            final int to = Utils.popLsb(ptr);
            list[index++] = Move.create(from, to, Move.CAPTURE_FLAG);
        }
        
        return index;
    }

    private int black(short[] list, int index, Board board) {
        return index;
    }
}
