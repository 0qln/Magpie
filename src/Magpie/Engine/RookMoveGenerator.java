package Engine;

import static Engine.Utils.*;

public class RookMoveGenerator extends MoveGenerator {

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
            final long mask, final long pieces, final long rooks,
            final int flag) {
        long[] r = { rooks }, toBB = { 0 };
        while (r[0] != 0) {
            final int from = popLsb(r);
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
                board.getBitboard(PieceType.Rook, color),
                Move.QUIET_MOVE_FLAG);
    }

    public int generateCaptures(short[] list, int index, Board board, int color) {
        return generate(
                list,
                index,
                board.getCBitboard(Color.NOT(color)),
                board.getCBitboard(Color.Black) | board.getCBitboard(Color.White),
                board.getBitboard(PieceType.Rook, color),
                Move.CAPTURE_FLAG);
    }

    // TODO: Replace with magic bitboards
    private long attacks(int square, long occupied) {
        final long fileBB = Masks.Files[square % 8];
        final long rankBB = Masks.Ranks[square / 8];
        final long nortBB = Utils.splitBBNorth(square);
        final long soutBB = Utils.splitBBSouth(square);

        long occupands, moves, result = 0, ray;
        int nearest;

        // South
        ray = fileBB & soutBB;
        occupands = occupied & ray;
        nearest = msb(occupands);
        moves = shift(splitBBNorth(nearest), CompassRose.Sout) & ray;
        result |= moves;

        // North
        ray = fileBB & nortBB;
        occupands = occupied & ray;
        nearest = lsb(occupands);
        moves = shift(splitBBSouth(nearest), CompassRose.Nort) & ray;
        result |= moves;

        // West
        ray = rankBB & soutBB;
        occupands = occupied & ray;
        nearest = msb(occupands);
        moves = shift(splitBBNorth(nearest), CompassRose.West) & ray;
        result |= moves;

        // East
        ray = rankBB & nortBB;
        occupands = occupied & ray;
        nearest = lsb(occupands);
        moves = shift(splitBBSouth(nearest), CompassRose.East) & ray;
        result |= moves;

        return result;
    }

}
