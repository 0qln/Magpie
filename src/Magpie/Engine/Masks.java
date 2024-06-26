package Engine;

import static Engine.Utils.*;

public final class Masks {

    public static final long[] Files = {
            0x0101010101010101L,
            0x0202020202020202L,
            0x0404040404040404L,
            0x0808080808080808L,
            0x1010101010101010L,
            0x2020202020202020L,
            0x4040404040404040L,
            0x8080808080808080L,
    };
    
    public static final long[] Passants = {
            0x0202020202020202L,
            0x0505050505050505L,
            0x0a0a0a0a0a0a0a0aL,
            0x1414141414141414L,
            0x2828282828282828L,
            0x5050505050505050L,
            0xa0a0a0a0a0a0a0a0L,
            0x4040404040404040L,
    };

    public static final long[] Ranks = {
            0x00000000000000FFL,
            0x000000000000FF00L,
            0x0000000000FF0000L,
            0x00000000FF000000L,
            0x000000FF00000000L,
            0x0000FF0000000000L,
            0x00FF000000000000L,
            0xFF00000000000000L,
    };

    public static final long[] Diags_A1H8 = {
            0x0100000000000000L,
            0x0201000000000000L,
            0x0402010000000000L,
            0x0804020100000000L,
            0x1008040201000000L,
            0x2010080402010000L,
            0x4020100804020100L,
            0x8040201008040201L,
            0x0080402010080402L,
            0x0000804020100804L,
            0x0000008040201008L,
            0x0000000080402010L,
            0x0000000000804020L,
            0x0000000000008040L,
            0x0000000000000080L,
    };

    public static final long[] Diags_A8H1 = {
            0x0000000000000001L,
            0x0000000000000102L,
            0x0000000000010204L,
            0x0000000001020408L,
            0x0000000102040810L,
            0x0000010204081020L,
            0x0001020408102040L,
            0x0102040810204080L,
            0x0204081020408000L,
            0x0408102040800000L,
            0x0810204080000000L,
            0x1020408000000000L,
            0x2040800000000000L,
            0x4080000000000000L,
            0x8000000000000000L,
    };
    
    public static final long RelevantOccupancy = 0X7E7E7E7E7E7E00l;
    
    public static final long[] RelevantFiles = {
        0x01010101010100L,
        0x02020202020200L,
        0x04040404040400L,
        0x08080808080800L,
        0x10101010101000L,
        0x20202020202000L,
        0x40404040404000L,
        0x80808080808000L,
    };

    public static final long[] RelevantRanks = {
        0x7eL,
        0x7e00L,
        0x7e0000L,
        0x7e000000L,
        0x7e00000000L,
        0x7e0000000000L,
        0x7e000000000000L,
        0x7e00000000000000L,
    };

    public static final long East = 0x7F7F7F7F7F7F7F7FL,
            EaEa = 0x3F3F3F3F3F3F3F3FL,
            West = 0xFEFEFEFEFEFEFEFEL,
            WeWe = 0xFCFCFCFCFCFCFCFCL,
            Sout = 0xFFFFFFFFFFFFFF00L,
            SoSo = 0xFFFFFFFFFFFF0000L,
            Nort = 0x00FFFFFFFFFFFFFFL,
            NoNo = 0x0000FFFFFFFFFFFFL;

    public static final long NoEaEa = Nort & EaEa,
            NoNoEa = NoNo & East,
            NoWeWe = Nort & WeWe,
            NoNoWe = NoNo & West,
            SoEaEa = Sout & EaEa,
            SoSoEa = SoSo & East,
            SoWeWe = Sout & WeWe,
            SoSoWe = SoSo & West;

    static {
        // Precompute all rays
        long[][] rays = new long[64][64];
        for (int i = 0; i < 64; i++) {
            for (int j = 0; j < 64; j++) {

                SlidingPiece.MoveGenerator gen;

                if (rank(i) == rank(j) || file(i) == file(j)) {
                    gen = Rook.generator;
                } else if (diagA1H8(i) == diagA1H8(j) || diagA8H1(i) == diagA8H1(j)) {
                    gen = Bishop.generator;
                } else {
                    continue;
                }

                long iBB = gen.attacks(i, 0);
                long jBB = gen.attacks(j, 0);
                rays[i][j] = target(i) | target(j) | (iBB & jBB);

            }
        }
        RAYS = rays;
    }

    public static final long[][] RAYS;

    public static final long ray(int square1, int square2) {
        return RAYS[square1][square2];
    }

    public static final long squaresBetween(int square1, int square2) {
        int northernSquare, southernSquare;
        if (square1 > square2) {
            northernSquare = square1;
            southernSquare = square2;
        } else {
            southernSquare = square1;
            northernSquare = square2;
        }
        long ray = RAYS[square1][square2];
        // printBB(ray);
        return ray & splitBBSouth(northernSquare) & splitBBNorth(southernSquare);
    }
}
