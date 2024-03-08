package Misc;

import java.util.ArrayList;
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
        
    public static <TSource, TResult> ArrayList<TResult> select(TSource[] source, java.util.function.Function<TSource, TResult> func) {
        ArrayList<TResult> result = new ArrayList<>(source.length);
        for (int i = 0; i < source.length; i++) {
            result.add(i, func.apply(source[i]));
        }
        return result;
    }

    public static void copyTo(int destoffset, short[] source, short[] desination) {
        assert(source.length <= desination.length + destoffset);

        for (int i = destoffset; i < source.length; i++) {
            desination[i] = source[i-destoffset];
        }
    }

    public static int lerp(int v0, int v1, int t, int tMax) {
        return ((tMax - t) * v0 + t * v1) / tMax;
    }


    public static <T> void copyTo(int destoffset, T[] source, T[] desination) {
        assert(source.length <= desination.length + destoffset);

        for (int i = destoffset; i < source.length; i++) {
            desination[i] = source[i-destoffset];
        }
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
