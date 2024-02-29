package Engine;

public final class CompassRose {

    public static final int 

        // Ray directions
        Nort = +8, 
        Sout = -8, 
        East = +1,
        West = -1,
        SoWe = Sout + West,
        NoWe = Nort + West,
        SoEa = Sout + East,
        NoEa = Nort + East,        

        // Knight directions
        NoNoWe = 2 * Nort + West,
        NoNoEa = 2 * Nort + East,
        NoWeWe = Nort + 2 * West,
        NoEaEa = Nort + 2 * East,
        SoSoWe = 2 * Sout + West,
        SoSoEa = 2 * Sout + East,
        SoWeWe = Sout + 2 * West,
        SoEaEa = Sout + 2 * East ;

}
