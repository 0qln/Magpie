package Engine;

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

    public static final long 
        East = 0x7F7F7F7F7F7F7F7FL,
        EaEa = 0x3F3F3F3F3F3F3F3FL,
        West = 0xFEFEFEFEFEFEFEFEL,
        WeWe = 0xFCFCFCFCFCFCFCFCL,
        Sout = 0xFFFFFFFFFFFFFF00L,
        SoSo = 0xFFFFFFFFFFFF0000L,
        Nort = 0x00FFFFFFFFFFFFFFL,
        NoNo = 0x0000FFFFFFFFFFFFL;

    public static final long 
        NoEaEa = Nort & EaEa,
        NoNoEa = NoNo & East,
        NoWeWe = Nort & WeWe,
        NoNoWe = NoNo & West,
        SoEaEa = Sout & EaEa,
        SoSoEa = SoSo & East,
        SoWeWe = Sout & WeWe,
        SoSoWe = SoSo & West;
}
