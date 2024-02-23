package Engine;

public final class Utils
{
    public static int toSquareIndex(String square) {
        int file = square.charAt(0) - 'a';
        int rank = square.charAt(1) - '1';
        return sqaureIndex0(rank, file);
    } 

    public static int sqaureIndex0(int rank, int file) {
        return file + 8 * rank;
    }

    public static long deactivateBit(long board, int squareIdx) {
        return board & ~(1L << squareIdx);
    }

    public static long activateBit(long board, int squareIdx) {
        return board | (1L << squareIdx);
    }

    public static long flipBit(long board, int squareIdx) {
        return board ^ (1L << squareIdx);
    }

    public static long flipBits(long board, int squareIdx1, int squareIdx2) {
        return board ^ (1L << squareIdx1 | 1L << squareIdx2);
    }

    public static boolean getBit(long board, int squareIdx) {
        return ((board >> squareIdx) & 1) == 1;
    }

    public static long setBit(long board, int squareIdx, boolean value) {
        return (board & ~(1L << squareIdx)) | ((value ? 1L : 0L) << squareIdx);
    }

    public static void deactivateBit(long[] boards, int index, int squareIdx) {
        boards[index] &= ~(1L << squareIdx);
    }

    public static void activateBit(long[] boards, int index, int squareIdx) {
        boards[index] |= (1L << squareIdx);
    }

    public static void flipBit(long[] boards, int index, int squareIdx) {
        boards[index] ^= (1L << squareIdx);
    }

    public static void flipBits(long[] boards, int index, int squareIdx1, int squareIdx2) {
        boards[index] ^= (1L << squareIdx1 | 1L << squareIdx2);
    }

    public static boolean getBit(long[] boards, int index, int squareIdx) {
        return ((boards[index] >> squareIdx) & 1) == 1;
    }

    public static void setBit(long[] boards, int index, int squareIdx, boolean value) {
        boards[index] = (boards[index] & ~(1L << squareIdx)) | ((value ? 1L : 0L) << squareIdx);
    }
}
