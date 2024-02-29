package Engine;

public final class Utils
{
    

    public static final long deactivateBit(long board, int squareIdx) {
        return board & ~(1L << squareIdx);
    }

    public static final long activateBit(long board, int squareIdx) {
        return board | (1L << squareIdx);
    }

    public static final long flipBit(long board, int squareIdx) {
        return board ^ (1L << squareIdx);
    }

    public static final long flipBits(long board, int squareIdx1, int squareIdx2) {
        return board ^ (1L << squareIdx1 | 1L << squareIdx2);
    }

    public static final boolean getBit(long board, int squareIdx) {
        return ((board >>> squareIdx) & 1) == 1;
    }

    public static final long setBit(long board, int squareIdx, boolean value) {
        return (board & ~(1L << squareIdx)) | ((value ? 1L : 0L) << squareIdx);
    }

    public static final void deactivateBit(long[] boards, int index, int squareIdx) {
        boards[index] &= ~(1L << squareIdx);
    }

    public static final void activateBit(long[] boards, int index, int squareIdx) {
        boards[index] |= (1L << squareIdx);
    }

    public static final void flipBit(long[] boards, int index, int squareIdx) {
        boards[index] ^= (1L << squareIdx);
    }

    public static final void flipBits(long[] boards, int index, int squareIdx1, int squareIdx2) {
        boards[index] ^= (1L << squareIdx1 | 1L << squareIdx2);
    }

    public static final boolean getBit(long[] boards, int index, int squareIdx) {
        return ((boards[index] >>> squareIdx) & 1) == 1;
    }

    public static final void setBit(long[] boards, int index, int squareIdx, boolean value) {
        boards[index] = (boards[index] & ~(1L << squareIdx)) | ((value ? 1L : 0L) << squareIdx);
    }

    // excluding the square itself
    public static final long splitBBNorth(int square) {
        return ~0L << square + 1;
    }
    public static final long splitBBSouth(int square) {
        return ~0L >>> (64 - square);
    }

    public static final int lsb(long board) {
        return board == 0 ? 0 : Long.numberOfTrailingZeros(board);
    }

    public static final int msb(long board) {
        return board == 0 ? 64 : 63 -  Long.numberOfLeadingZeros(board);
    }

    public static final int popLsb(long[] board) {
        int lsb = lsb(board[0]);
        board[0] &= board[0] - 1;
        return lsb;
    } 

    public static final long shift(long bb, int direction) {
        return direction > 0 ? bb << direction : bb >>> -direction; 
    }

    public static final long shift(long[] bb, int direction) {
        return shift(bb[0], direction);
    }

    public static final long target(int square) {
        return 1L << square;
    }

    public static final void printBB(long bb) {
        String result = "";
        for (int rank = 7; rank >= 0; rank--) {
            result += (rank + 1) + "  ";
            for (int file = 0; file <= 7; file++) {
                int square = Misc.Utils.sqaureIndex0(rank, file);
                char c = getBit(bb, square) ? 'x' : '.';
                result += c + " ";
            }
            result += "\n";
        }
        result += "   a b c d e f g h\n";
        System.out.println(result);
    }

    public static final void printBB(long[] bb) {
        printBB(bb[0]);
    }
}
