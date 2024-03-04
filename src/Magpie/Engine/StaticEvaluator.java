package Engine;

import static Engine.Utils.countBits;

public final class StaticEvaluator {

    public static int[] PT_VALUES = {
            0, 100, 300, 350, 500, 900, 10_000
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
        result += 15;

        return result;
    }

}
