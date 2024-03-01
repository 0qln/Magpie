package Engine;

import java.util.BitSet;

public final class Castling {

    public static final int QueenSide = PieceType.Queen, KingSide = PieceType.King;

    /*
     * [
     * 0b00: Queen, White
     * 0b01: King, White
     * 0b10: Queen, Black
     * 0b11: King, Black
     * ]
     */

    public static final BitSet empty() {
        return BitSet.valueOf(new byte[4]);
    }

    public static final BitSet create(
            boolean kingSideWhite,
            boolean queenSideWhite,
            boolean kingSideBlack,
            boolean queenSideBlack) {
        return BitSet.valueOf(new byte[] {
                fromBoolean(queenSideWhite),
                fromBoolean(kingSideWhite),
                fromBoolean(queenSideBlack),
                fromBoolean(kingSideBlack)
        });
    }

    public static final void set(BitSet castling, int side, int color, boolean value) {
        castling.set((side - 5) | (color << 1), value);
    }

    public static final boolean get(BitSet castling, int side, int color) {
        return castling.get((side - 5) | (color << 1));
    }

    private static final byte fromBoolean(boolean b) {
        return b ? (byte) 1 : (byte) 0;
    }
}
