package Engine;

// enums in java suck :D uwu
public final class Color {
    public static final int White = 0;
    public static final int Black = 1;

    public static int Not(int color) {
        return color == White ? Black : White;
    }
}