package Engine;

import static Engine.Utils.*;

public class BishopMoveGenerator extends MoveGenerator {

    @Override
    int generate(short[] list, int index, Board board, int color) {
        index = generateCaptures(list, index, board, color);
        index = generateQuiets(list, index, board, color);
        return index;
    }

    /*
     * Generate in chunks
     * https://www.chessprogramming.org/Move_Generation#Chunk_Move_Generation
     */

    /**
     * @param list
     * @param index
     * @param board
     * @param color
     * @param mask  Generate moves only for target squares, specified by this
     *              argument.
     * @return
     */
    public int generate(
            short[] list, int index,
            final long mask, final long pieces, final long bishops,
            final int flag) {
        long[] b = { bishops }, toBB = { 0 };
        while (b[0] != 0) {
            final int from = popLsb(b);
            toBB[0] = attacks(from, pieces) & mask;
            while (toBB[0] != 0)
                list[index++] = Move.create(from, popLsb(toBB), flag);
        }
        return index;
    }

    public int generateQuiets(short[] list, int index, Board board, int color) {
        final long pieces = board.getCBitboard(Color.Black) | board.getCBitboard(Color.White);
        return generate(
                list,
                index,
                ~pieces,
                pieces,
                board.getBitboard(PieceType.Bishop, color),
                Move.QUIET_MOVE_FLAG);
    }

    public int generateCaptures(short[] list, int index, Board board, int color) {
        return generate(
                list,
                index,
                board.getCBitboard(Color.NOT(color)),
                board.getCBitboard(Color.Black) | board.getCBitboard(Color.White),
                board.getBitboard(PieceType.Bishop, color),
                Move.CAPTURE_FLAG);
    }

    // TODO: Replace with magic bitboards
    private long attacks(final int square, final long occupied) {
        final int rank = square / 8, file = square % 8;
        final long a1h8BB = Masks.Diags_A1H8[7 - rank + file], a8h1BB = Masks.Diags_A8H1[rank + file];
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
