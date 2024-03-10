package Engine;

import java.util.BitSet;

public final class Castling {

    public static final int QueenSide = Queen.ID_Type;
    public static final int KingSide = King.ID_Type;

    public static final long[][] MoveMask = {
            new long[2],
            new long[2],
    };

    public static final long[][] CheckMask = {
            new long[2],
            new long[2],
    };

    public static final int[][] RookSquares = {
            new int[2], 
            new int[2],
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

        RookSquares[sideToIndex(KingSide)][Color.White]     = 07;
        RookSquares[sideToIndex(QueenSide)][Color.White]    = 00;
        RookSquares[sideToIndex(KingSide)][Color.Black]     = 63;
        RookSquares[sideToIndex(QueenSide)][Color.Black]    = 56;
    }

    public static final void update(BitSet castling, int captureSquare, int nstm) {
        if (captureSquare == RookSquares[sideToIndex(KingSide)][nstm]) {
            set(castling, KingSide, nstm, false);
        }
        if (captureSquare == RookSquares[sideToIndex(QueenSide)][nstm]) {
            set(castling, QueenSide, nstm, false);
        }
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
        BitSet result = new BitSet(4);
        set(result, KingSide, Color.White, kingSideWhite);
        set(result, QueenSide, Color.White, queenSideWhite);
        set(result, KingSide, Color.Black, kingSideBlack);
        set(result, QueenSide, Color.Black, queenSideBlack);
        return result;
    }

    public static final void set(BitSet castling, int side, int color, boolean value) {
        castling.set(toIndex(side, color), value);
    }

    public static final void set(BitSet castling, int color, boolean value) {
        castling.set(toIndex(QueenSide, color), value);
        castling.set(toIndex(KingSide, color), value);
    }

    public static final boolean get(BitSet castling, int side, int color) {
        return castling.get(toIndex(side, color));
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

    private static final int toIndex(int side, int color) {
        return (color << 1) | sideToIndex(side);
    }

    public static final String toString(BitSet castling) {
        String result = "" +
        (get(castling, KingSide, Color.White) ? "K" : "") +
        (get(castling, QueenSide, Color.White) ? "Q" : "") +
        (get(castling, KingSide, Color.Black) ? "k" : "") +
        (get(castling, QueenSide, Color.Black) ? "q" : "");
        return result.equals("") ? "-" : result;
    }
}
