
package Engine;

import static Engine.Utils.*;

public class Queen extends SlidingPiece {

    public static final MoveGenerator generator = new MoveGenerator();

    @Override
    public Engine.Piece.MoveGenerator getGenerator() {
        return generator;
    }

    public static class MoveGenerator extends SlidingPiece.MoveGenerator {

        @Override
        public long attacks(int square, long occupied) {
            return Rook.generator.attacks(square, occupied)
                    | Bishop.generator.attacks(square, occupied);
        }

        @Override
        public long attacks(int square) {
            return Rook.generator.attacks(square) | Bishop.generator.attacks(square);
        }

        @Override
        int generate(short[] list, int index, Board board, int color) {
            index = captures(list, index, board, color);
            index = quiets(list, index, board, color);
            return index;
        }


        public int quiets(short[] list, int index, Board board, int color) {
            return generate(
                    list,
                    index,
                    ~board.getOccupancy(),
                    board.getOccupancy(),
                    board.getBitboard(PieceType.Queen, color),
                    Move.QUIET_MOVE_FLAG);
        }

        public int captures(short[] list, int index, Board board, int color) {
            return generate(
                    list,
                    index,
                    board.getCBitboard(Color.NOT(color)),
                    board.getOccupancy(),
                    board.getBitboard(PieceType.Queen, color),
                    Move.CAPTURE_FLAG);
        }
    }

}