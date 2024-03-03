package Engine;

import static Engine.Utils.*;

public class Rook extends SlidingPiece {

    public static final int TypeID = 0;

    public static final MoveGenerator generator = new Rook.MoveGenerator();

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
            index = generateCaptures(list, index, board, color);
            index = quiets(list, index, board, color);
            return index;
        }

        public int quiets(short[] list, int index, Board board, int color) {

            // [Ignored case] in case of a double check, this function should not get called
            // assert (board.isInDoubleCheck() == false);

            return generate(
                    list,
                    index,
                    // only non-occupied squares
                    ~board.getOccupancy()
                    // [General restriction] when in check, allow only check blocking moves
                    // & (board.isInSingleCheck() ? board.getCheckBlockSqs() : ~0x0L)
                    ,
                    board.getOccupancy(),
                    board.getBitboard(PieceType.Rook, color),
                    Move.QUIET_MOVE_FLAG);
        }

        public int generateCaptures(short[] list, int index, Board board, int color) {

            // [Ignored case] in case of a double check, this function should not get called
            // assert (board.isInDoubleCheck() == false);

            return generate(
                    list,
                    index,
                    // only squares with enemy pieces
                    board.getCBitboard(Color.NOT(color))
                    // [General restriction] when in check, allow only check resolving captures
                    // & (board.isInSingleCheck() ? board.getCheckers() : ~0x0L)
                    ,
                    board.getOccupancy(),
                    board.getBitboard(PieceType.Rook, color),
                    Move.CAPTURE_FLAG);
        }

        @Override
        public long attacks(int square) {
            return (Masks.Files[file(square)] | Masks.Ranks[rank(square)]) ^ target(square);
        }

        // TODO: Replace with magic bitboards
        @Override
        public long attacks(int square, long occupied) {
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

}
