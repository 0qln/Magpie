package Engine;

import static Engine.Utils.*;

public class PawnMoveGenerator extends MoveGenerator
{
    // Indexed by color
    private static final long[] STEP2 = new long[] { Masks.Ranks[1], Masks.Ranks[6] };

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
        toBB[0] = shift(pawns, CompassRose.Nort);
        toBB[0] ^= toBB[0] & pieces; // Exclude occupied squares 
        fromBB[0] = shift(toBB, CompassRose.Sout);
        while (toBB[0] != 0) {
            from = popLsb(fromBB);
            to = popLsb(toBB);
            list[index++] = Move.create(from, to, Move.QUIET_MOVE_FLAG);
        }
        
        // Double step
        toBB[0] = shift(pawns & STEP2[Color.White], 2 * CompassRose.Nort);
        toBB[0] ^= toBB[0] & pieces; 
        toBB[0] ^= shift(shift(toBB, CompassRose.Sout) & pieces, CompassRose.Nort);
        fromBB[0] = shift(toBB, 2 * CompassRose.Sout);
        while (toBB[0] != 0) {
            from = popLsb(fromBB);
            to = popLsb(toBB);
            list[index++] = Move.create(from, to, Move.DOUBLE_PAWN_PUSH_FLAG);
        }

        // Capture right
        toBB[0] = shift(pawns & Masks.West, CompassRose.NoWe) & enemies;
        fromBB[0] = shift(toBB, CompassRose.NoWe);
        while (toBB[0] != 0) {
            from = popLsb(fromBB);
            to = popLsb(toBB);
            list[index++] = Move.create(from, to, Move.CAPTURE_FLAG);
        }
 
        // Capture left
        toBB[0] = shift(pawns & Masks.East, CompassRose.NoEa) & enemies;
        fromBB[0] = shift(toBB, CompassRose.SoWe);
        while (toBB[0] != 0) {
            from = popLsb(fromBB);
            to = popLsb(toBB);
            list[index++] = Move.create(from, to, Move.CAPTURE_FLAG);
        }
        
        // En passant 
        // Check left and right of the en passant square for an ally pawn
        toBB[0] = target(board.getEnPassantSquare());
        to = lsb(toBB[0]);
        fromBB[0] = shift(shift(pawns & Masks.East, CompassRose.NoEa) & toBB[0], CompassRose.SoWe); 
        fromBB[0] |= shift(shift(pawns & Masks.West, CompassRose.NoWe) & toBB[0], CompassRose.NoWe);
        while (fromBB[0] != 0) {
            from = popLsb(fromBB);
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
        toBB[0] = shift(pawns, CompassRose.Sout);
        toBB[0] ^= toBB[0] & pieces; // Exclude occupied squares 
        fromBB[0] = shift(toBB, CompassRose.Nort);
        while (toBB[0] != 0) {
            from = popLsb(fromBB);
            to = popLsb(toBB);
            list[index++] = Move.create(from, to, Move.QUIET_MOVE_FLAG);
        }
        
        // Double step
        toBB[0] = shift(pawns & STEP2[Color.Black], CompassRose.Sout * 2);
        toBB[0] ^= toBB[0] & pieces; 
        toBB[0] ^= shift(shift(toBB, CompassRose.Nort) & pieces, CompassRose.Sout);
        fromBB[0] = shift(toBB, CompassRose.Nort * 2);
        while (toBB[0] != 0) {
            from = popLsb(fromBB);
            to = popLsb(toBB);
            list[index++] = Move.create(from, to, Move.DOUBLE_PAWN_PUSH_FLAG);
        }

        // Capture right
        toBB[0] = shift(pawns & Masks.West, CompassRose.SoWe) & enemies;
        fromBB[0] = shift(toBB, CompassRose.NoEa);
        while (toBB[0] != 0) {
            from = popLsb(fromBB);
            to = popLsb(toBB);
            list[index++] = Move.create(from, to, Move.CAPTURE_FLAG);
        }
 
        // Capture left
        toBB[0] = shift(pawns & Masks.East, CompassRose.SoEa) & enemies;
        fromBB[0] = shift(toBB, CompassRose.NoWe);
        while (toBB[0] != 0) {
            from = popLsb(fromBB);
            to = popLsb(toBB);
            list[index++] = Move.create(from, to, Move.CAPTURE_FLAG);
        }

        // En passant 
        // Check left and right of the en passant square for an ally pawn
        toBB[0] = target(board.getEnPassantSquare());
        to = lsb(toBB[0]);
        fromBB[0] = shift(shift(pawns & Masks.East, CompassRose.SoEa) & toBB[0], CompassRose.NoWe); 
        fromBB[0] |= shift(shift(pawns & Masks.West, CompassRose.SoWe) & toBB[0], CompassRose.NoEa);
        while (fromBB[0] != 0) {
            from = popLsb(fromBB);
            list[index++] = Move.create(from, to, Move.EN_PASSANT_FLAG);
        }
       
        return index;
    }
}
