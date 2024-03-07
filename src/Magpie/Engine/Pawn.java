package Engine;

import static Engine.Utils.*;

public class Pawn extends Piece {

    public static final MoveGenerator generator = new MoveGenerator();

    @Override
    public Engine.Piece.MoveGenerator getGenerator() {
        return generator;
    }

    public static class MoveGenerator extends Piece.MoveGenerator {

        // Indexed by color
        private static final long[] STEP2 = new long[] { Masks.Ranks[1], Masks.Ranks[6] };
        private static final long[] PROMOTION_RANK = new long[] { 7, 0 };

        @Override
        int generate(short[] list, int index, Board board, int color, boolean capturesOnly) {
            return color == Color.White
                    ? white(list, index, board, capturesOnly)
                    : black(list, index, board, capturesOnly);
        }

        @Override
        int resolves(short[] list, int index, Board board, int color, boolean capturesOnly) {
            return color == Color.White
                    ? whiteResolves(list, index, board, capturesOnly)
                    : blackResolves(list, index, board, capturesOnly);
        }

        private int white(short[] list, int index, Board board, boolean capturesOnly) {
            final long pawns = board.getBitboard(PieceType.Pawn, Color.White);
            final long enemies = board.getCBitboard(Color.Black);
            final long pieces = enemies | board.getCBitboard(Color.White);
            int from, to;
            long[] toBB = new long[1];
            long[] fromBB = new long[1];

            if (!capturesOnly) {

                // Single step
                toBB[0] = shift(pawns, CompassRose.Nort);
                toBB[0] ^= toBB[0] & pieces; // Exclude occupied squares
                fromBB[0] = shift(toBB, CompassRose.Sout);
                while (toBB[0] != 0) {
                    from = popLsb(fromBB);
                    to = popLsb(toBB);
                    if (rank(to) == PROMOTION_RANK[Color.White])
                        // promotion
                        for (int flag = Move.PROMOTION_KNIGHT_FLAG; flag <= Move.PROMOTION_QUEEN_FLAG; flag++)
                            list[index++] = Move.create(from, to, flag);
                    else
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
            }

            // Capture west
            toBB[0] = shift(pawns & Masks.West, CompassRose.NoWe) & enemies;
            fromBB[0] = shift(toBB, CompassRose.SoEa);
            while (toBB[0] != 0) {
                from = popLsb(fromBB);
                to = popLsb(toBB);
                if (rank(to) == PROMOTION_RANK[Color.White])
                    // promotion
                    for (int flag = Move.CAPTURE_PROMOTION_KNIGHT_FLAG; flag <= Move.CAPTURE_PROMOTION_QUEEN_FLAG; flag++)
                        list[index++] = Move.create(from, to, flag);
                else
                    list[index++] = Move.create(from, to, Move.CAPTURE_FLAG);
            }

            // Capture east
            toBB[0] = shift(pawns & Masks.East, CompassRose.NoEa) & enemies;
            fromBB[0] = shift(toBB, CompassRose.SoWe);
            while (toBB[0] != 0) {
                from = popLsb(fromBB);
                to = popLsb(toBB);
                if (rank(to) == PROMOTION_RANK[Color.White])
                    // promotion
                    for (int flag = Move.CAPTURE_PROMOTION_KNIGHT_FLAG; flag <= Move.CAPTURE_PROMOTION_QUEEN_FLAG; flag++)
                        list[index++] = Move.create(from, to, flag);
                else
                    list[index++] = Move.create(from, to, Move.CAPTURE_FLAG);
            }

            // En passant
            // Check left and right of the en passant square for an ally pawn
            if (board.getEnPassantSquare() != -1) {
                toBB[0] = target(board.getEnPassantSquare());
                to = lsb(toBB[0]);
                fromBB[0] = shift(shift(pawns & Masks.East, CompassRose.NoEa) & toBB[0], CompassRose.SoWe);
                fromBB[0] |= shift(shift(pawns & Masks.West, CompassRose.NoWe) & toBB[0], CompassRose.SoEa);
                while (fromBB[0] != 0) {
                    from = popLsb(fromBB);
                    list[index++] = Move.create(from, to, Move.EN_PASSANT_FLAG);
                }
            }

            return index;
        }

        private int black(short[] list, int index, Board board, boolean capturesOnly) {
            final long pawns = board.getBitboard(PieceType.Pawn, Color.Black);
            final long enemies = board.getCBitboard(Color.White);
            final long pieces = enemies | board.getCBitboard(Color.Black);
            int from, to;
            long[] toBB = new long[1];
            long[] fromBB = new long[1];

            if (!capturesOnly) {

                // Single step
                toBB[0] = shift(pawns, CompassRose.Sout);
                toBB[0] ^= toBB[0] & pieces; // Exclude occupied squares
                fromBB[0] = shift(toBB, CompassRose.Nort);
                while (toBB[0] != 0) {
                    from = popLsb(fromBB);
                    to = popLsb(toBB);
                    if (rank(to) == PROMOTION_RANK[Color.Black])
                        // promotion
                        for (int flag = Move.PROMOTION_KNIGHT_FLAG; flag <= Move.PROMOTION_QUEEN_FLAG; flag++)
                            list[index++] = Move.create(from, to, flag);
                    else
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
            }

            // Capture west
            toBB[0] = shift(pawns & Masks.West, CompassRose.SoWe) & enemies;
            fromBB[0] = shift(toBB, CompassRose.NoEa);
            while (toBB[0] != 0) {
                from = popLsb(fromBB);
                to = popLsb(toBB);
                if (rank(to) == PROMOTION_RANK[Color.Black])
                    // promotion
                    for (int flag = Move.CAPTURE_PROMOTION_KNIGHT_FLAG; flag <= Move.CAPTURE_PROMOTION_QUEEN_FLAG; flag++)
                        list[index++] = Move.create(from, to, flag);
                else
                    list[index++] = Move.create(from, to, Move.CAPTURE_FLAG);
            }

            // Capture east
            toBB[0] = shift(pawns & Masks.East, CompassRose.SoEa) & enemies;
            fromBB[0] = shift(toBB, CompassRose.NoWe);
            while (toBB[0] != 0) {
                from = popLsb(fromBB);
                to = popLsb(toBB);
                if (rank(to) == PROMOTION_RANK[Color.Black])
                    // promotion
                    for (int flag = Move.CAPTURE_PROMOTION_KNIGHT_FLAG; flag <= Move.CAPTURE_PROMOTION_QUEEN_FLAG; flag++)
                        list[index++] = Move.create(from, to, flag);
                else
                    list[index++] = Move.create(from, to, Move.CAPTURE_FLAG);
            }

            // En passant
            // Check left and right of the en passant square for an ally pawn
            if (board.getEnPassantSquare() != -1) {
                toBB[0] = target(board.getEnPassantSquare());
                to = lsb(toBB[0]);
                fromBB[0] = shift(shift(pawns & Masks.East, CompassRose.SoEa) & toBB[0], CompassRose.NoWe);
                fromBB[0] |= shift(shift(pawns & Masks.West, CompassRose.SoWe) & toBB[0], CompassRose.NoEa);
                while (fromBB[0] != 0) {
                    from = popLsb(fromBB);
                    list[index++] = Move.create(from, to, Move.EN_PASSANT_FLAG);
                }
            }

            return index;
        }

        private int whiteResolves(short[] list, int index, Board board, boolean capturesOnly) {
            final long pawns = board.getBitboard(PieceType.Pawn, Color.White);
            final long pieces = board.getOccupancy();

            int from, to;
            long[] toBB = { 0 }, fromBB = { 0 };

            final long checkerBB = board.getCheckers();
            final int checker = lsb(checkerBB);
            final int king = lsb(board.getBitboard(PieceType.King, Color.White));
            final long mask = Masks.squaresBetween(king, checker);

            if (!capturesOnly) {

                // Single step
                toBB[0] = shift(pawns, CompassRose.Nort) & mask;
                fromBB[0] = shift(toBB, CompassRose.Sout);
                while (toBB[0] != 0) {
                    from = popLsb(fromBB);
                    to = popLsb(toBB);
                    if (rank(to) == PROMOTION_RANK[Color.White])
                        // promotion
                        for (int flag = Move.PROMOTION_KNIGHT_FLAG; flag <= Move.PROMOTION_QUEEN_FLAG; flag++)
                            list[index++] = Move.create(from, to, flag);
                    else
                        list[index++] = Move.create(from, to, Move.QUIET_MOVE_FLAG);
                }

                // Double step
                toBB[0] = shift(pawns & STEP2[Color.White], 2 * CompassRose.Nort) & mask;
                toBB[0] ^= shift(shift(toBB, CompassRose.Sout) & pieces, CompassRose.Nort);
                fromBB[0] = shift(toBB, 2 * CompassRose.Sout);
                while (toBB[0] != 0)
                    list[index++] = Move.create(popLsb(fromBB), popLsb(toBB), Move.DOUBLE_PAWN_PUSH_FLAG);
            }

            // Capture west
            toBB[0] = shift(pawns & Masks.West, CompassRose.NoWe) & checkerBB;
            fromBB[0] = shift(toBB, CompassRose.SoEa);
            while (toBB[0] != 0) {
                from = popLsb(fromBB);
                to = popLsb(toBB);
                if (rank(to) == PROMOTION_RANK[Color.White])
                    // promotion
                    for (int flag = Move.CAPTURE_PROMOTION_KNIGHT_FLAG; flag <= Move.CAPTURE_PROMOTION_QUEEN_FLAG; flag++)
                        list[index++] = Move.create(from, to, flag);
                else
                    list[index++] = Move.create(from, to, Move.CAPTURE_FLAG);
            }

            // Capture east
            toBB[0] = shift(pawns & Masks.East, CompassRose.NoEa) & checkerBB;
            fromBB[0] = shift(toBB, CompassRose.SoWe);
            while (toBB[0] != 0) {
                from = popLsb(fromBB);
                to = popLsb(toBB);
                if (rank(to) == PROMOTION_RANK[Color.White])
                    // promotion
                    for (int flag = Move.CAPTURE_PROMOTION_KNIGHT_FLAG; flag <= Move.CAPTURE_PROMOTION_QUEEN_FLAG; flag++)
                        list[index++] = Move.create(from, to, flag);
                else
                    list[index++] = Move.create(from, to, Move.CAPTURE_FLAG);
            }

            // En passant
            if (board.getEnPassantSquare() != -1) {
                toBB[0] = target(board.getEnPassantSquare());
                if (shift(toBB[0], CompassRose.Sout) == checkerBB) {
                    to = lsb(toBB[0]);
                    fromBB[0] = shift(shift(pawns & Masks.East, CompassRose.NoEa) & toBB[0], CompassRose.SoWe);
                    fromBB[0] |= shift(shift(pawns & Masks.West, CompassRose.NoWe) & toBB[0], CompassRose.SoEa);
                    while (fromBB[0] != 0) {
                        from = popLsb(fromBB);
                        list[index++] = Move.create(from, to, Move.EN_PASSANT_FLAG);
                    }
                }
            }

            return index;
        }

        private int blackResolves(short[] list, int index, Board board, boolean capturesOnly) {
            final long pawns = board.getBitboard(PieceType.Pawn, Color.Black);
            final long pieces = board.getOccupancy();
            final long checkerBB = board.getCheckers();
            final int checker = lsb(checkerBB);
            final int king = lsb(board.getBitboard(PieceType.King, Color.Black));
            final long mask = Masks.squaresBetween(king, checker);
            int from, to;
            long[] toBB = { 0 }, fromBB = { 0 };

            if (!capturesOnly) {

                // Single step
                toBB[0] = shift(pawns, CompassRose.Sout) & mask;
                fromBB[0] = shift(toBB, CompassRose.Nort);
                while (toBB[0] != 0) {
                    from = popLsb(fromBB);
                    to = popLsb(toBB);
                    if (rank(to) == PROMOTION_RANK[Color.Black])
                        // promotion
                        for (int flag = Move.PROMOTION_KNIGHT_FLAG; flag <= Move.PROMOTION_QUEEN_FLAG; flag++)
                            list[index++] = Move.create(from, to, flag);
                    else
                        list[index++] = Move.create(from, to, Move.QUIET_MOVE_FLAG);
                }

                // Double step
                toBB[0] = shift(pawns & STEP2[Color.Black], 2 * CompassRose.Sout) & mask;
                toBB[0] ^= shift(shift(toBB, CompassRose.Nort) & pieces, CompassRose.Sout);
                fromBB[0] = shift(toBB, 2 * CompassRose.Nort);
                while (toBB[0] != 0)
                    list[index++] = Move.create(popLsb(fromBB), popLsb(toBB), Move.DOUBLE_PAWN_PUSH_FLAG);
            }

            // Capture west
            toBB[0] = shift(pawns & Masks.West, CompassRose.SoWe) & checkerBB;
            fromBB[0] = shift(toBB, CompassRose.NoEa);
            while (toBB[0] != 0) {
                from = popLsb(fromBB);
                to = popLsb(toBB);
                if (rank(to) == PROMOTION_RANK[Color.Black])
                    // promotion
                    for (int flag = Move.CAPTURE_PROMOTION_KNIGHT_FLAG; flag <= Move.CAPTURE_PROMOTION_QUEEN_FLAG; flag++)
                        list[index++] = Move.create(from, to, flag);
                else
                    list[index++] = Move.create(from, to, Move.CAPTURE_FLAG);
            }

            // Capture east
            toBB[0] = shift(pawns & Masks.East, CompassRose.SoEa) & checkerBB;
            fromBB[0] = shift(toBB, CompassRose.NoWe);
            while (toBB[0] != 0) {
                from = popLsb(fromBB);
                to = popLsb(toBB);
                if (rank(to) == PROMOTION_RANK[Color.Black])
                    // promotion
                    for (int flag = Move.CAPTURE_PROMOTION_KNIGHT_FLAG; flag <= Move.CAPTURE_PROMOTION_QUEEN_FLAG; flag++)
                        list[index++] = Move.create(from, to, flag);
                else
                    list[index++] = Move.create(from, to, Move.CAPTURE_FLAG);
            }

            // En passant
            if (board.getEnPassantSquare() != -1) {
                toBB[0] = target(board.getEnPassantSquare());
                if (shift(toBB[0], CompassRose.Nort) == checkerBB) {
                    fromBB[0] = (shift(toBB, CompassRose.NoWe) & pawns & Masks.East) |
                            (shift(toBB, CompassRose.NoEa) & pawns & Masks.West);
                    while (fromBB[0] != 0)
                        list[index++] = Move.create(popLsb(fromBB), board.getEnPassantSquare(), Move.EN_PASSANT_FLAG);
                }
            }

            return index;
        }

        @Override
        public final long attacks(int square, int color) {
            return ATTACKS[color][square];
        }

        @Override
        public final long attacks(int square, long occupied, int color) {
            return attacks(square, color);
        }

        private static final long[][] ATTACKS = {
                {
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
                },
                {
                        0, 0, 0, 0, 0, 0, 0, 0,
                        0x2L,
                        0x5L,
                        0xaL,
                        0x14L,
                        0x28L,
                        0x50L,
                        0xa0L,
                        0x40L,
                        0x200L,
                        0x500L,
                        0xa00L,
                        0x1400L,
                        0x2800L,
                        0x5000L,
                        0xa000L,
                        0x4000L,
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
                        0, 0, 0, 0, 0, 0, 0, 0,
                }
        };
    }

}
