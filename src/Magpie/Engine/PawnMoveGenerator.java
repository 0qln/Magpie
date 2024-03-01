package Engine;

import static Engine.Utils.*;

public class PawnMoveGenerator extends MoveGenerator {
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

    public static final long attacks(int square) {
        return ATTACKS[square];
    }

    private static final long[] ATTACKS = {
            0, 0, 0, 0, 0, 0, 0, 0,
            0x20000L,
            0x50000L,
            0xa0000L,
            0x140000L,
            0x280000L,
            0x500000L,
            0xa00000L,
            0x400000L,
            0x2000000L,
            0x5000000L,
            0xa000000L,
            0x14000000L,
            0x28000000L,
            0x50000000L,
            0xa0000000L,
            0x40000000L,
            0x200000000L,
            0x500000000L,
            0xa00000000L,
            0x1400000000L,
            0x2800000000L,
            0x5000000000L,
            0xa000000000L,
            0x4000000000L,
            0x20000000000L,
            0x50000000000L,
            0xa0000000000L,
            0x140000000000L,
            0x280000000000L,
            0x500000000000L,
            0xa00000000000L,
            0x400000000000L,
            0x2000000000000L,
            0x5000000000000L,
            0xa000000000000L,
            0x14000000000000L,
            0x28000000000000L,
            0x50000000000000L,
            0xa0000000000000L,
            0x40000000000000L,
            0x200000000000000L,
            0x500000000000000L,
            0xa00000000000000L,
            0x1400000000000000L,
            0x2800000000000000L,
            0x5000000000000000L,
            0xa000000000000000L,
            0x4000000000000000L,
            0, 0, 0, 0, 0, 0, 0, 0,
    };
}
