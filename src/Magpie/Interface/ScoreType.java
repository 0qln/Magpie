package Interface;

public enum ScoreType {
    CentiPawns, Mate, Lowerbound, UpperBound;

    public static String toString(ScoreType value) {
        switch (value) {
            case CentiPawns: return "cp";
            case Mate: return "mate";
            case Lowerbound: return "lowerbound";
            case UpperBound: return "upperbound"; 
            default: return null;
        }
    }
}
