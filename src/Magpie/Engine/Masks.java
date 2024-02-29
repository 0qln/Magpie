package Engine;

public final class Masks {

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
