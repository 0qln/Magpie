package Engine;

import static Engine.Utils.*;
import Misc.Utils;

public class King extends PieceType {

    public static final MoveGenerator generator = new MoveGenerator();
    public static final int ID_Type = 6;
    public static final int ID_White = Piece.create(ID_Type, Color.White);
    public static final int ID_Black = Piece.create(ID_Type, Color.Black);

    @Override
    public MoveGenerator getGenerator() {
        return generator;
    }

    public static class MoveGenerator extends PieceType.MoveGenerator {

        @Override
        int generate(short[] list, int index, Board board, int color, boolean capturesOnly) {
            index = captures(list, index, board, color);
            if (!capturesOnly) {
                index = generateCastling(list, index, board, color);
                index = quiets(list, index, board, color);
            }
            return index;
        }

        @Override
        int resolves(short[] list, int index, Board board, int color, boolean capturesOnly) {
            index = captures(list, index, board, color);
            if (!capturesOnly)
                index = quiets(list, index, board, color);
            return index;
        }

        public int generate(
                short[] list, int index,
                final long mask, final long king,
                final int flag) {
            final int from = lsb(king);
            final long[] toBB = { attacks(from, -1) & mask };
            while (toBB[0] != 0)
                list[index++] = Move.create(from, popLsb(toBB), flag);
            return index;
        }

        public int generateCastling(short[] list, int index, Board board, int color) {
            final int rank = color == Color.White ? 0 : 7;
            final int from = Utils.sqaureIndex0(rank, Files.E);
            if (board.canCastle(Castling.KingSide, color))
                list[index++] = Move.create(from, Utils.sqaureIndex0(rank, Files.G), Move.KING_CASTLE_FLAG);
            if (board.canCastle(Castling.QueenSide, color))
                list[index++] = Move.create(from, Utils.sqaureIndex0(rank, Files.C), Move.QUEEN_CASTLE_FLAG);
            return index;
        }

        public int quiets(short[] list, int index, Board board, int color) {
            return generate(
                    list,
                    index,
                    ~board.getOccupancy(),
                    board.getBitboard(King.ID_Type, color),
                    Move.QUIET_MOVE_FLAG);
        }

        public int captures(short[] list, int index, Board board, int color) {
            return generate(
                    list,
                    index,
                    board.getCBitboard(Color.NOT(color)),
                    board.getBitboard(King.ID_Type, color),
                    Move.CAPTURE_FLAG);
        }

        @Override
        public final long attacks(int square, int color) {
            return ATTACKS[square];
        }

        @Override
        public final long attacks(int square, long occupied, int color) {
            return attacks(square, color);
        }

        private static final long[] ATTACKS = {
                0x302L,
                0x705L,
                0xe0aL,
                0x1c14L,
                0x3828L,
                0x7050L,
                0xe0a0L,
                0xc040L,
                0x30203L,
                0x70507L,
                0xe0a0eL,
                0x1c141cL,
                0x382838L,
                0x705070L,
                0xe0a0e0L,
                0xc040c0L,
                0x3020300L,
                0x7050700L,
                0xe0a0e00L,
                0x1c141c00L,
                0x38283800L,
                0x70507000L,
                0xe0a0e000L,
                0xc040c000L,
                0x302030000L,
                0x705070000L,
                0xe0a0e0000L,
                0x1c141c0000L,
                0x3828380000L,
                0x7050700000L,
                0xe0a0e00000L,
                0xc040c00000L,
                0x30203000000L,
                0x70507000000L,
                0xe0a0e000000L,
                0x1c141c000000L,
                0x382838000000L,
                0x705070000000L,
                0xe0a0e0000000L,
                0xc040c0000000L,
                0x3020300000000L,
                0x7050700000000L,
                0xe0a0e00000000L,
                0x1c141c00000000L,
                0x38283800000000L,
                0x70507000000000L,
                0xe0a0e000000000L,
                0xc040c000000000L,
                0x302030000000000L,
                0x705070000000000L,
                0xe0a0e0000000000L,
                0x1c141c0000000000L,
                0x3828380000000000L,
                0x7050700000000000L,
                0xe0a0e00000000000L,
                0xc040c00000000000L,
                0x203000000000000L,
                0x507000000000000L,
                0xa0e000000000000L,
                0x141c000000000000L,
                0x2838000000000000L,
                0x5070000000000000L,
                0xa0e0000000000000L,
                0x40c0000000000000L,
        };

    }

}
