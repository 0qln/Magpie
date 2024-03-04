package Engine;

import java.util.BitSet;

public final class Castling {

    public static final int QueenSide = PieceType.Queen;
    public static final int KingSide = PieceType.King;

    public static final long[][] MoveMask = {
            new long[2],
            new long[2],
    };

    public static final long[][] CheckMask = {
            new long[2],
            new long[2],
    };

    static {
        MoveMask[sideToIndex(KingSide)][Color.White]    = 0x60L;
        MoveMask[sideToIndex(QueenSide)][Color.White]   = 0xEL;
        MoveMask[sideToIndex(KingSide)][Color.Black]    = 0x6000000000000000L;
        MoveMask[sideToIndex(QueenSide)][Color.Black]   = 0xE00000000000000L;
        CheckMask[sideToIndex(KingSide)][Color.White]    = 0x60L;
        CheckMask[sideToIndex(QueenSide)][Color.White]   = 0xCL;
        CheckMask[sideToIndex(KingSide)][Color.Black]    = 0x6000000000000000L;
        CheckMask[sideToIndex(QueenSide)][Color.Black]   = 0xC00000000000000L;
    }

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
        castling.set(sideToIndex(side) | (color << 1), value);
    }

    public static final boolean get(BitSet castling, int side, int color) {
        return castling.get(sideToIndex(side) | (color << 1));
    }

    private static final byte fromBoolean(boolean b) {
        return b ? (byte) 1 : (byte) 0;
    }

    public static final boolean hasSpace(int side, int color, long boardOccupancy) {
        return (boardOccupancy & MoveMask[sideToIndex(side)][color]) == 0;
    }

    public static final boolean hasNerve(int side, int color, long nstmAttacks) {
        return (nstmAttacks & CheckMask[sideToIndex(side)][color]) == 0;
    }

    private static final int sideToIndex(int side) {
        assert (side == QueenSide || side == KingSide);
        return side - 5;
    }
}
