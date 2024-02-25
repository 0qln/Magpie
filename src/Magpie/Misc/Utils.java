package Misc;

import java.util.function.Predicate;

public final class Utils
{
    public static <T> int indexOf(T[] array, Predicate<T> predicate) {
        for (int i = 0; i < array.length; i++) {
            if (predicate.test(array[i])) {
                return i;
            }
        }
        return -1;
    }

    public static String fromSquareIndex(int square) {
        char file = (char)((square % 8) + 'a');
        char rank = (char)((square / 8) + '1');
        return String.valueOf(new char[] { file, rank } );
    }

    public static int toSquareIndex(String square) {
        int file = square.charAt(0) - 'a';
        int rank = square.charAt(1) - '1';
        return sqaureIndex0(rank, file);
    } 

    public static int sqaureIndex0(int rank, int file) {
        return file + 8 * rank;
    }

}
