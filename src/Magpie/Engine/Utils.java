package Engine;

import java.util.function.Predicate;

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
}
