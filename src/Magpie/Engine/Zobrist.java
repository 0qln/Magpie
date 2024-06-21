package Engine;

import static Engine.Utils.file;

import java.util.Random;

// https://www.chessprogramming.org/Zobrist_Hashing

public class Zobrist {

    public static final long[][] pieceSQ = new long[14][64];
    // public static final long[] enPassant = new long[9]; // Index 0 is for EP_SQ == -1
    public static final long[] enPassant = new long[8];
    public static final long[] castling = new long[16];
    public static long stm;

    public static void init(long seed) {
        Random rng = new Random(seed);

        for (int sq = 0; sq < 64; sq++) {
            for (int piece = 0; piece < 14; piece++) {
                pieceSQ[piece][sq] = rng.nextLong();
            }
        }

        for (int i = 0; i < castling.length; i++) {
            castling[i] = rng.nextLong();
        }

        for (int i = 0; i < enPassant.length; i++) {
            enPassant[i] = rng.nextLong();
        }

        stm = rng.nextLong();
    }

    public static long initialKey(Board board, BoardState.Builder state) {
        long key = 0;

        for (int sq = 0; sq < 64; sq++)
            key ^= pieceSQ[board.getPieceID(sq)][sq];

        // // If the ep square is a valid square (0-63), the snipped will return the
        // // correct file + 1. Else if there is no ep square (-1), the snipped will return 0
        // // ([-1 % 8] + 1 = 0).
        // key ^= enPassant[file(epSQ) + 1];
        int epSQ = state.getEpSquare();
        key ^= epSQ == -1 ? 0 : enPassant[file(epSQ)];

        if (board.getTurn() == Color.Black)
            key ^= stm;

        key ^= castling[Castling.Key(state.getCastling())];

        return key; 
    }

    // public static long stmUpdate(int stm) {
    //     // return stm == Color.Black ? stm : 0;
    //     return stm;
    // }

    private static final void testCastling() {
        for (boolean ksw : new boolean[] {true, false})
        for (boolean qsw : new boolean[] {true, false})
        for (boolean ksb : new boolean[] {true, false})
        for (boolean qsb : new boolean[] {true, false})
            System.out.println(Castling.Key(Castling.create(ksw, qsw, ksb, qsb)));
    }
}
