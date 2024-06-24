package Engine;

import static Engine.Utils.*;

public abstract class SlidingPiece extends PieceType {

    public static abstract class MoveGenerator extends PieceType.MoveGenerator {
        
        protected abstract MoveGenerator _getInstance();

        public abstract long attacks(int square);

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
                toBB[0] = this.attacks(from, pieces, -1) & mask;
                while (toBB[0] != 0)
                    list[index++] = Move.create(from, popLsb(toBB), flag);
            }
            return index;
        }


        public long relevantOccupancy(int square) {
            long relevantOccupancy =  
                // Pieces that aren't in the bishops diagonals aren't relevant.
                attacks(square) 
                // Pieces that are on the outer edges aren't relevant.
                & Masks.RelevantOccupancy;
                   ; 
            return relevantOccupancy;
        }
        
        public long mapBits(int index, long mask) {
            long[] m = {mask};
            long result = 0;
            for (int bit = 0; m[0] != 0; bit++) {
                long value = (index >> bit) & 1;
                result |= value << popLsb(m);
            }
            return result;
        }
        
        // iterates the relevant occupancy
        public long nextRelevantOccupied(int square, long index) {
            long[] rlvt = { _getInstance().relevantOccupancy(square) };

            // Bounds check
            int idxMax = 1 << countBits(rlvt[0]);
            if (index >= idxMax)
                return -1;

            long result = 0;

            // map the bits of the index to the indeces of 
            // the relevant bits of the result.
            int bit = 0;
            while (rlvt[0] != 0) {
                long value = (index >> bit++) & 1;
                result |= value << popLsb(rlvt); 
            }
            
            return result;
        }

    }

}
