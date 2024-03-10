package Engine;

import static Engine.CompassRose.*;
import static Engine.Move.*;
import static Engine.Utils.*;

public class Pawn extends PieceType {

    public static final MoveGenerator generator = new MoveGenerator();
    public static final int ID_Type = 1;
    public static final int ID_White = Piece.create(ID_Type, Color.White);
    public static final int ID_Black = Piece.create(ID_Type, Color.Black);

    @Override
    public Engine.PieceType.MoveGenerator getGenerator() {
        return generator;
    }

    public static class MoveGenerator extends PieceType.MoveGenerator {

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
            final long pawns = board.getBitboard(Pawn.ID_Type, Color.White);
            final long enemies = board.getCBitboard(Color.Black);
            final long pieces = enemies | board.getCBitboard(Color.White);
            long[] toBB = { 0 }, fromBB = { 0 };

            if (!capturesOnly) {
                // Single step
                long blockers = shift(pieces, Sout);
                toBB[0] = shift(pawns & ~blockers, Nort);
                fromBB[0] = shift(toBB, Sout);
                while (toBB[0] != 0)
                    index = append(list, index, popLsb(fromBB), popLsb(toBB), Color.White,
                            QUIET_MOVE_FLAG, PROMOTION_KNIGHT_FLAG, PROMOTION_QUEEN_FLAG);

                // Double step
                blockers |= shift(pieces, Sout * 2);
                toBB[0] = shift(pawns & STEP2[Color.White] & ~blockers, 2 * Nort);
                fromBB[0] = shift(toBB, 2 * Sout);
                while (toBB[0] != 0)
                    list[index++] = create(popLsb(fromBB), popLsb(toBB), DOUBLE_PAWN_PUSH_FLAG);
            }

            // Capture west
            toBB[0] = shift(pawns & Masks.West, NoWe) & enemies;
            fromBB[0] = shift(toBB, SoEa);
            while (toBB[0] != 0)
                index = append(list, index, popLsb(fromBB), popLsb(toBB), Color.White,
                        CAPTURE_FLAG, CAPTURE_PROMOTION_KNIGHT_FLAG, CAPTURE_PROMOTION_QUEEN_FLAG);

            // Capture east
            toBB[0] = shift(pawns & Masks.East, NoEa) & enemies;
            fromBB[0] = shift(toBB, SoWe);
            while (toBB[0] != 0)
                index = append(list, index, popLsb(fromBB), popLsb(toBB), Color.White,
                        CAPTURE_FLAG, CAPTURE_PROMOTION_KNIGHT_FLAG, CAPTURE_PROMOTION_QUEEN_FLAG);

            // En passant
            // Check left and right of the en passant square for an ally pawn
            if (board.getEnPassantSquare() != -1) {
                toBB[0] = target(board.getEnPassantSquare());
                fromBB[0] = shift(shift(pawns & Masks.East, NoEa) & toBB[0], SoWe);
                fromBB[0] |= shift(shift(pawns & Masks.West, NoWe) & toBB[0], SoEa);
                while (fromBB[0] != 0)
                    list[index++] = create(popLsb(fromBB), board.getEnPassantSquare(), EN_PASSANT_FLAG);
            }

            return index;
        }

        private int append(short[] list, int index, int from, int to, int color, int flag, int promotionFlagMin,
                int promotionFlagMax) {
            if (rank(to) == PROMOTION_RANK[color])
                for (flag = promotionFlagMin; flag <= promotionFlagMax; flag++)
                    list[index++] = create(from, to, flag);
            else
                list[index++] = create(from, to, flag);
            return index;
        }

        private int black(short[] list, int index, Board board, boolean capturesOnly) {
            final long pawns = board.getBitboard(Pawn.ID_Type, Color.Black);
            final long enemies = board.getCBitboard(Color.White);
            final long pieces = enemies | board.getCBitboard(Color.Black);
            long[] toBB = { 0 }, fromBB = { 0 };

            if (!capturesOnly) {
                // Single step
                long blockers = shift(pieces, Nort);
                toBB[0] = shift(pawns & ~blockers, Sout);
                fromBB[0] = shift(toBB, Nort);
                while (toBB[0] != 0)
                    index = append(list, index, popLsb(fromBB), popLsb(toBB), Color.Black,
                            QUIET_MOVE_FLAG, PROMOTION_KNIGHT_FLAG, PROMOTION_QUEEN_FLAG);

                // Double step
                blockers |= shift(pieces, Nort * 2);
                toBB[0] = shift(pawns & STEP2[Color.Black] & ~blockers, Sout * 2);
                fromBB[0] = shift(toBB, Nort * 2);
                while (toBB[0] != 0)
                    list[index++] = create(popLsb(fromBB), popLsb(toBB), DOUBLE_PAWN_PUSH_FLAG);
            }

            // Capture west
            toBB[0] = shift(pawns & Masks.West, SoWe) & enemies;
            fromBB[0] = shift(toBB, NoEa);
            while (toBB[0] != 0)
                index = append(list, index, popLsb(fromBB), popLsb(toBB), Color.Black,
                        CAPTURE_FLAG, CAPTURE_PROMOTION_KNIGHT_FLAG, CAPTURE_PROMOTION_QUEEN_FLAG);

            // Capture east
            toBB[0] = shift(pawns & Masks.East, SoEa) & enemies;
            fromBB[0] = shift(toBB, NoWe);
            while (toBB[0] != 0)
                index = append(list, index, popLsb(fromBB), popLsb(toBB), Color.Black,
                        CAPTURE_FLAG, CAPTURE_PROMOTION_KNIGHT_FLAG, CAPTURE_PROMOTION_QUEEN_FLAG);

            // En passant
            // Check left and right of the en passant square for an ally pawn
            if (board.getEnPassantSquare() != -1) {
                toBB[0] = target(board.getEnPassantSquare());
                fromBB[0] = shift(shift(pawns & Masks.East, SoEa) & toBB[0], NoWe);
                fromBB[0] |= shift(shift(pawns & Masks.West, SoWe) & toBB[0], NoEa);
                while (fromBB[0] != 0) {
                    list[index++] = create(popLsb(fromBB), board.getEnPassantSquare(), EN_PASSANT_FLAG);
                }
            }

            return index;
        }

        private int whiteResolves(short[] list, int index, Board board, boolean capturesOnly) {
            final long pawns = board.getBitboard(Pawn.ID_Type, Color.White);
            final long pieces = board.getOccupancy();
            final long checkerBB = board.getCheckers();
            final int checker = lsb(checkerBB);
            final int king = lsb(board.getBitboard(King.ID_Type, Color.White));
            final long mask = Masks.squaresBetween(king, checker);
            long[] toBB = { 0 }, fromBB = { 0 };

            if (!capturesOnly) {
                // Single step
                toBB[0] = shift(pawns, Nort) & mask;
                fromBB[0] = shift(toBB, Sout);
                while (toBB[0] != 0)
                    index = append(list, index, popLsb(fromBB), popLsb(toBB), Color.White,
                            QUIET_MOVE_FLAG, PROMOTION_KNIGHT_FLAG, PROMOTION_QUEEN_FLAG);

                // Double step
                toBB[0] = shift(pawns & STEP2[Color.White] & ~shift(pieces, Sout), 2 * Nort) & mask;
                fromBB[0] = shift(toBB, 2 * Sout);
                while (toBB[0] != 0)
                    list[index++] = create(popLsb(fromBB), popLsb(toBB), DOUBLE_PAWN_PUSH_FLAG);
            }

            // Capture west
            toBB[0] = shift(pawns & Masks.West, NoWe) & checkerBB;
            fromBB[0] = shift(toBB, SoEa);
            while (toBB[0] != 0)
                index = append(list, index, popLsb(fromBB), popLsb(toBB), Color.White,
                        CAPTURE_FLAG, CAPTURE_PROMOTION_KNIGHT_FLAG, CAPTURE_PROMOTION_QUEEN_FLAG);

            // Capture east
            toBB[0] = shift(pawns & Masks.East, NoEa) & checkerBB;
            fromBB[0] = shift(toBB, SoWe);
            while (toBB[0] != 0)
                index = append(list, index, popLsb(fromBB), popLsb(toBB), Color.White,
                        CAPTURE_FLAG, CAPTURE_PROMOTION_KNIGHT_FLAG, CAPTURE_PROMOTION_QUEEN_FLAG);

            // En passant
            if (board.getEnPassantSquare() != -1) {
                toBB[0] = target(board.getEnPassantSquare());
                if (shift(toBB[0], Sout) == checkerBB) {
                    fromBB[0] = shift(shift(pawns & Masks.East, NoEa) & toBB[0], SoWe);
                    fromBB[0] |= shift(shift(pawns & Masks.West, NoWe) & toBB[0], SoEa);
                    while (fromBB[0] != 0)
                        list[index++] = create(popLsb(fromBB), board.getEnPassantSquare(), EN_PASSANT_FLAG);
                }
            }

            return index;
        }

        private int blackResolves(short[] list, int index, Board board, boolean capturesOnly) {
            final long pawns = board.getBitboard(Pawn.ID_Type, Color.Black);
            final long pieces = board.getOccupancy();
            final long checkerBB = board.getCheckers();
            final int checker = lsb(checkerBB);
            final int king = lsb(board.getBitboard(King.ID_Type, Color.Black));
            final long mask = Masks.squaresBetween(king, checker);
            long[] toBB = { 0 }, fromBB = { 0 };

            if (!capturesOnly) {
                // Single step
                toBB[0] = shift(pawns, Sout) & mask;
                fromBB[0] = shift(toBB, Nort);
                while (toBB[0] != 0)
                    index = append(list, index, popLsb(fromBB), popLsb(toBB), Color.Black,
                            QUIET_MOVE_FLAG, PROMOTION_KNIGHT_FLAG, PROMOTION_QUEEN_FLAG);

                // Double step
                toBB[0] = shift(pawns & STEP2[Color.Black] &~ shift(pieces, Nort), 2 * Sout) & mask;
                fromBB[0] = shift(toBB, 2 * Nort);
                while (toBB[0] != 0)
                    list[index++] = create(popLsb(fromBB), popLsb(toBB), DOUBLE_PAWN_PUSH_FLAG);
            }

            // Capture west
            toBB[0] = shift(pawns & Masks.West, SoWe) & checkerBB;
            fromBB[0] = shift(toBB, NoEa);
            while (toBB[0] != 0)
                index = append(list, index, popLsb(fromBB), popLsb(toBB), Color.Black,
                        CAPTURE_FLAG, CAPTURE_PROMOTION_KNIGHT_FLAG, CAPTURE_PROMOTION_QUEEN_FLAG);

            // Capture east
            toBB[0] = shift(pawns & Masks.East, SoEa) & checkerBB;
            fromBB[0] = shift(toBB, NoWe);
            while (toBB[0] != 0)
                index = append(list, index, popLsb(fromBB), popLsb(toBB), Color.Black,
                        CAPTURE_FLAG, CAPTURE_PROMOTION_KNIGHT_FLAG, CAPTURE_PROMOTION_QUEEN_FLAG);

            // En passant
            if (board.getEnPassantSquare() != -1) {
                toBB[0] = target(board.getEnPassantSquare());
                if (shift(toBB[0], Nort) == checkerBB) {
                    fromBB[0] = (shift(toBB, NoWe) & pawns & Masks.East) |
                            (shift(toBB, NoEa) & pawns & Masks.West);
                    while (fromBB[0] != 0)
                        list[index++] = create(popLsb(fromBB), board.getEnPassantSquare(), EN_PASSANT_FLAG);
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
