package Engine;

import static Engine.Utils.countBits;
import static Engine.Utils.popLsb;

public final class StaticEvaluator {

    public static final int Infinity = 40_000, Checkmate = 39_000, Draw = 0;

    public static final int PHASE_MG = 0, PHASE_EG = 1;

    public static final int[][] PT_VALUES = {
            // mg
            { 0, 100, 300, 350, 500, 900, 10_000 },
            // eg
            { 0, 200, 350, 450, 550, 950, 10_000 },
    };

    public static final int getPieceSquareBonus(int color, int square, int phase, int pt) {
        return PSQT_VALUES[pt][phase][color == Color.White ? square : square ^ 56];
    }

    // [PT][PHASE][SQUARE]
    // TODO: different values for mg and eg
    public static final int[][][] PSQT_VALUES = {
            // None piece
            {},
            {

                    // Pawns
                    {
                            0, 0, 0, 0, 0, 0, 0, 0,
                            50, 50, 50, 50, 50, 50, 50, 50,
                            10, 10, 20, 30, 30, 20, 10, 10,
                            5, 5, 10, 25, 25, 10, 5, 5,
                            0, 0, 0, 20, 20, 0, 0, 0,
                            5, -5, -10, 0, 0, -10, -5, 5,
                            5, 10, 10, -20, -20, 10, 10, 5,
                            0, 0, 0, 0, 0, 0, 0, 0
                    },
                    {
                            0, 0, 0, 0, 0, 0, 0, 0,
                            50, 50, 50, 50, 50, 50, 50, 50,
                            10, 10, 20, 30, 30, 20, 10, 10,
                            5, 5, 10, 25, 25, 10, 5, 5,
                            0, 0, 0, 20, 20, 0, 0, 0,
                            5, -5, -10, 0, 0, -10, -5, 5,
                            5, 10, 10, -20, -20, 10, 10, 5,
                            0, 0, 0, 0, 0, 0, 0, 0
                    },
            },
            {
                    // Knights
                    {
                            -50, -40, -30, -30, -30, -30, -40, -50,
                            -40, -20, 0, 0, 0, 0, -20, -40,
                            -30, 0, 10, 15, 15, 10, 0, -30,
                            -30, 5, 15, 20, 20, 15, 5, -30,
                            -30, 0, 15, 20, 20, 15, 0, -30,
                            -30, 5, 10, 15, 15, 10, 5, -30,
                            -40, -20, 0, 5, 5, 0, -20, -40,
                            -50, -40, -30, -30, -30, -30, -40, -50,
                    },
                    {
                            -50, -40, -30, -30, -30, -30, -40, -50,
                            -40, -20, 0, 0, 0, 0, -20, -40,
                            -30, 0, 10, 15, 15, 10, 0, -30,
                            -30, 5, 15, 20, 20, 15, 5, -30,
                            -30, 0, 15, 20, 20, 15, 0, -30,
                            -30, 5, 10, 15, 15, 10, 5, -30,
                            -40, -20, 0, 5, 5, 0, -20, -40,
                            -50, -40, -30, -30, -30, -30, -40, -50,
                    },
            },
            {
                    // Bihsops
                    {
                            -20, -10, -10, -10, -10, -10, -10, -20,
                            -10, 0, 0, 0, 0, 0, 0, -10,
                            -10, 0, 5, 10, 10, 5, 0, -10,
                            -10, 5, 5, 10, 10, 5, 5, -10,
                            -10, 0, 10, 10, 10, 10, 0, -10,
                            -10, 10, 10, 10, 10, 10, 10, -10,
                            -10, 5, 0, 0, 0, 0, 5, -10,
                            -20, -10, -10, -10, -10, -10, -10, -20,
                    },
                    {
                            -20, -10, -10, -10, -10, -10, -10, -20,
                            -10, 0, 0, 0, 0, 0, 0, -10,
                            -10, 0, 5, 10, 10, 5, 0, -10,
                            -10, 5, 5, 10, 10, 5, 5, -10,
                            -10, 0, 10, 10, 10, 10, 0, -10,
                            -10, 10, 10, 10, 10, 10, 10, -10,
                            -10, 5, 0, 0, 0, 0, 5, -10,
                            -20, -10, -10, -10, -10, -10, -10, -20,
                    },
            },
            {
                    // Rooks
                    {
                            0, 0, 0, 0, 0, 0, 0, 0,
                            5, 10, 10, 10, 10, 10, 10, 5,
                            -5, 0, 0, 0, 0, 0, 0, -5,
                            -5, 0, 0, 0, 0, 0, 0, -5,
                            -5, 0, 0, 0, 0, 0, 0, -5,
                            -5, 0, 0, 0, 0, 0, 0, -5,
                            -5, 0, 0, 0, 0, 0, 0, -5,
                            0, 0, 0, 5, 5, 0, 0, 0
                    },
                    {
                            0, 0, 0, 0, 0, 0, 0, 0,
                            5, 10, 10, 10, 10, 10, 10, 5,
                            -5, 0, 0, 0, 0, 0, 0, -5,
                            -5, 0, 0, 0, 0, 0, 0, -5,
                            -5, 0, 0, 0, 0, 0, 0, -5,
                            -5, 0, 0, 0, 0, 0, 0, -5,
                            -5, 0, 0, 0, 0, 0, 0, -5,
                            0, 0, 0, 5, 5, 0, 0, 0
                    },
            },
            {
                    // Queens
                    {
                            -20, -10, -10, -5, -5, -10, -10, -20,
                            -10, 0, 0, 0, 0, 0, 0, -10,
                            -10, 0, 5, 5, 5, 5, 0, -10,
                            -5, 0, 5, 5, 5, 5, 0, -5,
                            0, 0, 5, 5, 5, 5, 0, -5,
                            -10, 5, 5, 5, 5, 5, 0, -10,
                            -10, 0, 5, 0, 0, 0, 0, -10,
                            -20, -10, -10, -5, -5, -10, -10, -20
                    },
                    {
                            -20, -10, -10, -5, -5, -10, -10, -20,
                            -10, 0, 0, 0, 0, 0, 0, -10,
                            -10, 0, 5, 5, 5, 5, 0, -10,
                            -5, 0, 5, 5, 5, 5, 0, -5,
                            0, 0, 5, 5, 5, 5, 0, -5,
                            -10, 5, 5, 5, 5, 5, 0, -10,
                            -10, 0, 5, 0, 0, 0, 0, -10,
                            -20, -10, -10, -5, -5, -10, -10, -20
                    }
            },
            {
                    // Kings
                    {
                            -30, -40, -40, -50, -50, -40, -40, -30,
                            -30, -40, -40, -50, -50, -40, -40, -30,
                            -30, -40, -40, -50, -50, -40, -40, -30,
                            -30, -40, -40, -50, -50, -40, -40, -30,
                            -20, -30, -30, -40, -40, -30, -30, -20,
                            -10, -20, -20, -20, -20, -20, -20, -10,
                            20, 20, 0, 0, 0, 0, 20, 20,
                            20, 30, 10, 0, 0, 10, 30, 20
                    },
                    {
                            -50, -40, -30, -20, -20, -30, -40, -50,
                            -30, -20, -10, 0, 0, -10, -20, -30,
                            -30, -10, 20, 30, 30, 20, -10, -30,
                            -30, -10, 30, 40, 40, 30, -10, -30,
                            -30, -10, 30, 40, 40, 30, -10, -30,
                            -30, -10, 20, 30, 30, 20, -10, -30,
                            -30, -30, 0, 0, 0, 0, -30, -30,
                            -50, -30, -30, -30, -30, -30, -30, -50
                    }
            }
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

        result += material(board, us, phase);
        result += psqt(board, us, phase);

        result -= material(board, nus, phase);
        result -= psqt(board, nus, phase);

        return result;
    }

    public static final int psqt(Board board, int color, int phase) {
        int result = 0;
        for (int pt = PieceType.Pawn; pt <= PieceType.King; pt++) {
            long[] pieces = { board.getBitboard(pt, color) };
            while (pieces[0] != 0)
                result += PSQT_VALUES[pt][phase][(color == Color.Black ? popLsb(pieces) : popLsb(pieces) ^ 56)];
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
        return PHASE_MAX - result;
    }

    public final static int taper(int phase, int mg, int eg) {
        return Misc.Utils.lerp(mg, eg, phase, PHASE_MAX);
    }

}
