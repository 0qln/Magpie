package Engine;

import static Engine.Utils.*;

public class Bishop extends SlidingPiece {

    public static final MoveGenerator generator = new Bishop.MoveGenerator();

    @Override
    public MoveGenerator getGenerator() {
        return generator;
    }

    public static class MoveGenerator extends SlidingPiece.MoveGenerator {

        /*
         * Generate in chunks
         * https://www.chessprogramming.org/Move_Generation#Chunk_Move_Generation
         */

        @Override
        int generate(short[] list, int index, Board board, int color) {
            index = captures(list, index, board, color);
            index = quiets(list, index, board, color);
            return index;
        }

        public int quiets(short[] list, int index, Board board, int color) {
            final long pieces = board.getCBitboard(Color.Black) | board.getCBitboard(Color.White);
            return generate(
                    list,
                    index,
                    ~pieces,
                    pieces,
                    board.getBitboard(PieceType.Bishop, color),
                    Move.QUIET_MOVE_FLAG);
        }

        public int captures(short[] list, int index, Board board, int color) {
            return generate(
                    list,
                    index,
                    board.getCBitboard(Color.NOT(color)),
                    board.getCBitboard(Color.Black) | board.getCBitboard(Color.White),
                    board.getBitboard(PieceType.Bishop, color),
                    Move.CAPTURE_FLAG);
        }

        @Override
        public long attacks(int square) {
            final long a1h8BB = Masks.Diags_A1H8[diagA1H8(square)], a8h1BB = Masks.Diags_A8H1[diagA8H1(square)];
            return (a1h8BB | a8h1BB) ^ target(square);
        }

        @Override
        public long attacks(int square, long occupied) {
            final long a1h8BB = Masks.Diags_A1H8[diagA1H8(square)], a8h1BB = Masks.Diags_A8H1[diagA8H1(square)];
            final long nortBB = Utils.splitBBNorth(square), soutBB = Utils.splitBBSouth(square);

            long occupands, moves, result = 0, ray;
            int nearest;

            // South East
            ray = a8h1BB & soutBB;
            occupands = occupied & ray;
            nearest = msb(occupands);
            moves = shift(splitBBNorth(nearest), CompassRose.SoEa) & ray;
            result |= moves;

            // South West
            ray = a1h8BB & soutBB;
            occupands = occupied & ray;
            nearest = msb(occupands);
            moves = shift(splitBBNorth(nearest), CompassRose.SoWe) & ray;
            result |= moves;

            // North West
            ray = a8h1BB & nortBB;
            occupands = occupied & ray;
            nearest = lsb(occupands);
            moves = shift(splitBBSouth(nearest), CompassRose.NoWe) & ray;
            result |= moves;

            // North East
            ray = a1h8BB & nortBB;
            occupands = occupied & ray;
            nearest = lsb(occupands);
            moves = shift(splitBBSouth(nearest), CompassRose.NoEa) & ray;
            result |= moves;

            return result;
        }

    }

}
