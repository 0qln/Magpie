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

    public static final int update(int castling, int captureSquare, int nstm) {
        if (captureSquare == RookSquares[sideToIndex(KingSide)][nstm]) {
            return setFalse(castling, KingSide, nstm);
        }
        if (captureSquare == RookSquares[sideToIndex(QueenSide)][nstm]) {
            return setFalse(castling, QueenSide, nstm);
        }
        return castling;
    }

    /*
     * [
     * 0b00: Queen, White
     * 0b01: King, White
     * 0b10: Queen, Black
     * 0b11: King, Black
     * ]
     */

    public static final int empty() {
        return 0;
    }

    public static final int create(
            boolean kingSideWhite,
            boolean queenSideWhite,
            boolean kingSideBlack,
            boolean queenSideBlack) {
        return create(
            kingSideWhite ? 1 : 0, 
            queenSideWhite ? 1 : 0, 
            kingSideBlack ? 1 : 0, 
            queenSideBlack ? 1 : 0);
    }

    public static final int create(
            int kingSideWhite,
            int queenSideWhite,
            int kingSideBlack,
            int queenSideBlack) {
        int result = 0;
        result = setValue(result, KingSide, Color.White, kingSideWhite);
        result = setValue(result, QueenSide, Color.White, queenSideWhite);
        result = setValue(result, KingSide, Color.Black, kingSideBlack);
        result = setValue(result, QueenSide, Color.Black, queenSideBlack);
        return result;
    }

    public static final int setFalse(int castling, int side, int color) {
        return castling & ~(1 << toIndex(side, color));
    }

    public static final int setTrue(int castling, int side, int color) {
        return castling | (1 << toIndex(side, color));
    }
    
    public static final int setValue(int castling, int side, int color, int maybe) {
        return castling | (maybe << toIndex(side, color));
    }

    public static final int get(int castling, int side, int color) {
        return 1 & (castling >> toIndex(side, color));
    }

    public static final boolean getB(int castling, int side, int color) {
        return get(castling, side, color) == 1;
    }

    public static int key(int castling) {
        return castling;
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

    public static final String toString(int castling) {
        String result = "" +
        (get(castling, KingSide, Color.White) == 1 ? "K" : "") +
        (get(castling, QueenSide, Color.White) == 1 ? "Q" : "") +
        (get(castling, KingSide, Color.Black) == 1 ? "k" : "") +
        (get(castling, QueenSide, Color.Black) == 1 ? "q" : "");
        return result.equals("") ? "-" : result;
    }
}
