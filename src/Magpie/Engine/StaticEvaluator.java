package Engine;

import static Engine.Utils.countBits;

public final class StaticEvaluator {

    public static final int Infinity = 40_000, Checkmate = 39_000, Draw = 0;

    public static final int PHASE_MG = 0, PHASE_EG = 1;

    public static final int[][] PT_VALUES = {
            // mg
            { 0, 100, 300, 350, 500, 900, 10_000 },
            // eg
            { 0, 200, 350, 450, 550, 950, 10_000 },
    };

    // eye balled
    public static final int[] BISHOP_PAIR = { 60, 100 };

    public static final int PHASE_MAX = 24;
    public static final int[] PHASE_VALUES = {
            0, 0, 1, 1, 2, 4, 0
    };

    public static final int evaluate(Board board, int relativeTo) {
        return taper(
                phase(board),
                evaluate(board, relativeTo, PHASE_MG),
                evaluate(board, relativeTo, PHASE_EG));
    }

    public static final int evaluate(Board board, int relativeTo, int phase) {
        final int us = relativeTo, nus = Color.NOT(us);

        int result = 0;

        // Evaluate stuff from the enemy, then turn the score 
        // around and evaluate our stuff.
        for (int color : new int[] { nus, us }) {
            result += material(board, color, phase);
            result += bishopPair(board, color, phase);

            // loop
            result = -result;
        }

        return result;
    }

    public static final int bishopPair(Board board, int color, int phase) {
        return countBits(board.getBitboard(PieceType.Bishop, color)) >= 2 ? BISHOP_PAIR[phase] : 0;
    }

    public static final int material(Board board, int color, int phase) {
        int result = 0;
        for (int pt = PieceType.Pawn; pt <= PieceType.Queen; pt++)
            result += countBits(board.getBitboard(pt, color)) * PT_VALUES[phase][pt];
        return result;
    }

    public final static int phase(Board board) {
        int result = 0;
        for (int pt = PieceType.Knight; pt <= PieceType.Queen; pt++)
            result += PHASE_VALUES[pt] * countBits(board.getTBitboard(pt));
        return result;
    }

    public final static int taper(int phase, int mg, int eg) {
        return Misc.Utils.lerp(mg, eg, phase, PHASE_MAX);
    }

}
