package Engine;

import static Engine.Utils.*;

public class Knight extends PieceType {

    public static final MoveGenerator generator = new MoveGenerator();
    public static final int ID_Type = 2;
    public static final int ID_White = Piece.create(ID_Type, Color.White);
    public static final int ID_Black = Piece.create(ID_Type, Color.Black);

    @Override
    public Engine.PieceType.MoveGenerator getGenerator() {
        return generator;
    }

    public static class MoveGenerator extends PieceType.MoveGenerator {

        @Override
        int generate(short[] list, int index, Board board, int color, boolean capturesOnly) {

            long knights = board.getBitboard(Knight.ID_Type, color);

            index = addMoves(list, index, board, color, knights & Masks.NoEaEa, CompassRose.NoEaEa, capturesOnly);
            index = addMoves(list, index, board, color, knights & Masks.SoEaEa, CompassRose.SoEaEa, capturesOnly);
            index = addMoves(list, index, board, color, knights & Masks.SoWeWe, CompassRose.SoWeWe, capturesOnly);
            index = addMoves(list, index, board, color, knights & Masks.NoWeWe, CompassRose.NoWeWe, capturesOnly);
            index = addMoves(list, index, board, color, knights & Masks.NoNoEa, CompassRose.NoNoEa, capturesOnly);
            index = addMoves(list, index, board, color, knights & Masks.SoSoEa, CompassRose.SoSoEa, capturesOnly);
            index = addMoves(list, index, board, color, knights & Masks.SoSoWe, CompassRose.SoSoWe, capturesOnly);
            index = addMoves(list, index, board, color, knights & Masks.NoNoWe, CompassRose.NoNoWe, capturesOnly);

            return index;
        }

        @Override
        int resolves(short[] list, int index, Board board, int color, boolean capturesOnly) {
            long knights = board.getBitboard(Knight.ID_Type, color);

            final int checker = lsb(board.getCheckers());
            final int king = lsb(board.getBitboard(King.ID_Type, color));
            final long mask = Masks.squaresBetween(king, checker);

            index = addResolves(list, index, board, color, knights & Masks.NoEaEa, CompassRose.NoEaEa, capturesOnly, checker, mask);
            index = addResolves(list, index, board, color, knights & Masks.SoEaEa, CompassRose.SoEaEa, capturesOnly, checker, mask);
            index = addResolves(list, index, board, color, knights & Masks.SoWeWe, CompassRose.SoWeWe, capturesOnly, checker, mask);
            index = addResolves(list, index, board, color, knights & Masks.NoWeWe, CompassRose.NoWeWe, capturesOnly, checker, mask);
            index = addResolves(list, index, board, color, knights & Masks.NoNoEa, CompassRose.NoNoEa, capturesOnly, checker, mask);
            index = addResolves(list, index, board, color, knights & Masks.SoSoEa, CompassRose.SoSoEa, capturesOnly, checker, mask);
            index = addResolves(list, index, board, color, knights & Masks.SoSoWe, CompassRose.SoSoWe, capturesOnly, checker, mask);
            index = addResolves(list, index, board, color, knights & Masks.NoNoWe, CompassRose.NoNoWe, capturesOnly, checker, mask);

            return index;
        }

        private int addResolves(short[] list, int index, Board board, int color, long knights, int dir, boolean capturesOnly, int checker, long mask) {
            // Remove all knights whose dest square is occupied by an ally.
            final long sao = shift(board.getCBitboard(color), -dir);
            final long[] fromBB = { knights & ~sao };

            while (fromBB[0] != 0) {
                final int from = popLsb(fromBB);
                final int to = from + dir;
                final int flag = board.getPieceID(to) == None.ID_White
                        ? Move.QUIET_MOVE_FLAG
                        : Move.CAPTURE_FLAG;
                if (capturesOnly && flag == Move.QUIET_MOVE_FLAG)
                    continue;
                if (checker == to || (target(to) & mask) != 0) 
                    list[index++] = Move.create(from, to, flag);
            }

            return index;
        }

        private int addMoves(short[] list, int index, Board board, int color, long knights, int dir, boolean capturesOnly) {
            // Remove all knights whose dest square is occupied by an ally.
            final long sao = shift(board.getCBitboard(color), -dir);
            final long[] fromBB = { knights & ~sao };

            while (fromBB[0] != 0) {
                final int from = popLsb(fromBB);
                final int to = from + dir;
                final int flag = board.getPieceID(to) == None.ID_White
                        ? Move.QUIET_MOVE_FLAG
                        : Move.CAPTURE_FLAG;
                if (capturesOnly && flag == Move.QUIET_MOVE_FLAG)
                    continue;
                list[index++] = Move.create(from, to, flag);
            }

            return index;
        }

        @Override
        public final long attacks(int square, int color) {
            return _attacks[square];
        }

        @Override
        public final long attacks(int square, long occupied, int color) {
            return attacks(square, color);
        }

        private static final long[] _attacks = {
                0x20400L,
                0x50800L,
                0xa1100L,
                0x142200L,
                0x284400L,
                0x508800L,
                0xa01000L,
                0x402000L,
                0x2040004L,
                0x5080008L,
                0xa110011L,
                0x14220022L,
                0x28440044L,
                0x50880088L,
                0xa0100010L,
                0x40200020L,
                0x204000402L,
                0x508000805L,
                0xa1100110aL,
                0x1422002214L,
                0x2844004428L,
                0x5088008850L,
                0xa0100010a0L,
                0x4020002040L,
                0x20400040200L,
                0x50800080500L,
                0xa1100110a00L,
                0x142200221400L,
                0x284400442800L,
                0x508800885000L,
                0xa0100010a000L,
                0x402000204000L,
                0x2040004020000L,
                0x5080008050000L,
                0xa1100110a0000L,
                0x14220022140000L,
                0x28440044280000L,
                0x50880088500000L,
                0xa0100010a00000L,
                0x40200020400000L,
                0x204000402000000L,
                0x508000805000000L,
                0xa1100110a000000L,
                0x1422002214000000L,
                0x2844004428000000L,
                0x5088008850000000L,
                0xa0100010a0000000L,
                0x4020002040000000L,
                0x400040200000000L,
                0x800080500000000L,
                0x1100110a00000000L,
                0x2200221400000000L,
                0x4400442800000000L,
                0x8800885000000000L,
                0x100010a000000000L,
                0x2000204000000000L,
                0x4020000000000L,
                0x8050000000000L,
                0x110a0000000000L,
                0x22140000000000L,
                0x44280000000000L,
                0x88500000000000L,
                0x10a00000000000L,
                0x20400000000000L,
        };
    }

}
