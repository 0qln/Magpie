package Engine;

import static Engine.Utils.*;

public abstract class SlidingPiece extends Piece {

    public static abstract class MoveGenerator extends Piece.MoveGenerator {

        public abstract long attacks(int square, long occupied);

        /**
         * @param list
         * @param index
         * @param board
         * @param color
         * @param mask  Generate moves only for target squares, specified by this
         *              argument.
         * @return
         */
        public <TGen extends SlidingPiece.MoveGenerator> int generate(
                short[] list, int index,
                final long mask, final long pieces, final long sliders,
                final int flag) {
            long[] b = { sliders }, toBB = { 0 };
            while (b[0] != 0) {
                final int from = popLsb(b);
                toBB[0] = this.attacks(from, pieces) & mask;
                while (toBB[0] != 0)
                    list[index++] = Move.create(from, popLsb(toBB), flag);
            }
            return index;
        }

    }

}
