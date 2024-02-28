package Engine;

public class PawnMoveGenerator extends MoveGenerator
{
    private static final long LEFT = 0x7F7F7F7F7F7F7F7FL;
    private static final long RIGHT = 0xFEFEFEFEFEFEFEFEL;
    // Indexed by color
    private static final long[] STEP2 = new long[] { 0xFF00L, 0xFF000000000000L };
    // Indexed by color
    private static final long[] EP = new long[] { 0xFF0000L, 0xFF0000000000L };

    @Override
    int generate(short[] list, int index, Board board, int color) {
        return color == Color.White 
            ? white(list, index, board) 
            : black(list, index, board);
    }

    private int white(short[] list, int index, Board board) {
        final long pawns = board.getBitboard(PieceType.Pawn, Color.White);
        final long enemies = board.getCBitboard(Color.Black);
        final long pieces = enemies | board.getCBitboard(Color.White);
        int from, to;
        long[] toBB = new long[1];
        long[] fromBB = new long[1];
        

        // Single step
        toBB[0] = (pawns << 8);
        toBB[0] ^= toBB[0] & pieces; // Exclude occupied squares 
        fromBB[0] = toBB[0] >>> 8;
        while (toBB[0] != 0) {
            from = Utils.popLsb(fromBB);
            to = Utils.popLsb(toBB);
            list[index++] = Move.create(from, to, Move.QUIET_MOVE_FLAG);
        }
        
        // Double step
        toBB[0] = ((pawns & STEP2[Color.White]) << 16);
        toBB[0] ^= toBB[0] & pieces; 
        toBB[0] ^= ((toBB[0] >>> 8) & pieces) << 8;
        fromBB[0] = toBB[0] >>> 16;
        while (toBB[0] != 0) {
            from = Utils.popLsb(fromBB);
            to = Utils.popLsb(toBB);
            list[index++] = Move.create(from, to, Move.DOUBLE_PAWN_PUSH_FLAG);
        }

        // Capture right
        toBB[0] = ((pawns & RIGHT) << 7) & enemies;
        fromBB[0] = toBB[0] >>> 7;
        while (toBB[0] != 0) {
            from = Utils.popLsb(fromBB);
            to = Utils.popLsb(toBB);
            list[index++] = Move.create(from, to, Move.CAPTURE_FLAG);
        }
 
        // Capture left
        toBB[0] = ((pawns & LEFT) << 9) & enemies;
        fromBB[0] = toBB[0] >>> 9;
        while (toBB[0] != 0) {
            from = Utils.popLsb(fromBB);
            to = Utils.popLsb(toBB);
            list[index++] = Move.create(from, to, Move.CAPTURE_FLAG);
        }
        
        // En passant 
        // Check left and right of the en passant square for an ally pawn
        toBB[0] = 1L << board.getEnPassantSquare();
        to = Utils.lsb(toBB[0]);
        fromBB[0] = (((pawns & LEFT) << 9) & toBB[0]) >>> 9; 
        fromBB[0] |= (((pawns & RIGHT) << 7) & toBB[0]) >>> 7;
        while (fromBB[0] != 0) {
            from = Utils.popLsb(fromBB);
            list[index++] = Move.create(from, to, Move.EN_PASSANT_FLAG);
        }

        return index;
    }

    private int black(short[] list, int index, Board board) {
        final long pawns = board.getBitboard(PieceType.Pawn, Color.Black);
        final long enemies = board.getCBitboard(Color.White);
        final long pieces = enemies | board.getCBitboard(Color.Black);
        int from, to;
        long[] toBB = new long[1];
        long[] fromBB = new long[1];
        

        // Single step
        toBB[0] = (pawns >>> 8);
        toBB[0] ^= toBB[0] & pieces; // Exclude occupied squares 
        fromBB[0] = toBB[0] << 8;
        while (toBB[0] != 0) {
            from = Utils.popLsb(fromBB);
            to = Utils.popLsb(toBB);
            list[index++] = Move.create(from, to, Move.QUIET_MOVE_FLAG);
        }
        
        // Double step
        toBB[0] = ((pawns & STEP2[Color.Black]) >>> 16);
        toBB[0] ^= toBB[0] & pieces; 
        toBB[0] ^= ((toBB[0] << 8) & pieces) >>> 8;
        fromBB[0] = toBB[0] << 16;
        while (toBB[0] != 0) {
            from = Utils.popLsb(fromBB);
            to = Utils.popLsb(toBB);
            list[index++] = Move.create(from, to, Move.DOUBLE_PAWN_PUSH_FLAG);
        }

        // Capture right
        toBB[0] = ((pawns & RIGHT) >>> 9) & enemies;
        fromBB[0] = toBB[0] << 9;
        while (toBB[0] != 0) {
            from = Utils.popLsb(fromBB);
            to = Utils.popLsb(toBB);
            list[index++] = Move.create(from, to, Move.CAPTURE_FLAG);
        }
 
        // Capture left
        toBB[0] = ((pawns & LEFT) >>> 7) & enemies;
        fromBB[0] = toBB[0] << 7;
        while (toBB[0] != 0) {
            from = Utils.popLsb(fromBB);
            to = Utils.popLsb(toBB);
            list[index++] = Move.create(from, to, Move.CAPTURE_FLAG);
        }

        // En passant 
        // Check left and right of the en passant square for an ally pawn
        toBB[0] = 1L << board.getEnPassantSquare();
        to = Utils.lsb(toBB[0]);
        fromBB[0] = (((pawns & LEFT) >>> 7) & toBB[0]) << 7; 
        fromBB[0] |= (((pawns & RIGHT) >>> 9) & toBB[0]) << 9;
        while (fromBB[0] != 0) {
            from = Utils.popLsb(fromBB);
            list[index++] = Move.create(from, to, Move.EN_PASSANT_FLAG);
        }
       
        return index;
    }
}
