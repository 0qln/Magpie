package Engine;

import static Engine.Utils.countBits;

public final class StaticEvaluator {

    public static final int Infinity = 40_000, Checkmate = 39_000, Draw = 0;

    public static final int[] PT_VALUES = {
            0, 100, 300, 350, 500, 900, 10_000
    };

    public static final int PHASE_TOTAL = 24;
    public static final int[] PHASE_VALUES = {
            0, 0, 1, 1, 2, 4, 0
    };

    private final Board _board;

    public StaticEvaluator(Board board) {
        this._board = board;
    }

    public final int evaluate(int relativeTo) {
        final int us = relativeTo, nus = Color.NOT(us);

        int result = 0;

        // Material
        for (int pt = PieceType.Pawn; pt <= PieceType.Queen; pt++) {
            result += countBits(_board.getBitboard(pt, us)) * PT_VALUES[pt];
            result -= countBits(_board.getBitboard(pt, nus)) * PT_VALUES[pt];
        }

        // Tempo
        // result += 15;

        return result;
    }

    public final static int phase(Board board) {
        int result = 0;
        result += PHASE_VALUES[PieceType.Knight] * countBits(board.getTBitboard(PieceType.Knight));
        result += PHASE_VALUES[PieceType.Bishop] * countBits(board.getTBitboard(PieceType.Bishop));
        result += PHASE_VALUES[PieceType.Rook] * countBits(board.getTBitboard(PieceType.Rook));
        result += PHASE_VALUES[PieceType.Queen] * countBits(board.getTBitboard(PieceType.Queen));
        return result;
    }

    // TODO: check if this is right
    public final static int taper(int phase, int mg, int eg) {
        return Misc.Utils.lerp(mg, eg, phase, PHASE_TOTAL);
    }


}
