package Engine;

public enum MoveFormat {
    /**
     * https://en.wikipedia.org/wiki/ICCF_numeric_notation
     */
    ICCF,

    /**
     * https://www.chessprogramming.org/Algebraic_Chess_Notation#Smith_Notation
     */
    Smith,
    
    /**
     * 
     * 
     * Common UCI input-output format.
     * 
     * eg. f1c4
     */
    LongAlgebraicNotation_UCI,

    /**
     * https://www.chessprogramming.org/Algebraic_Chess_Notation#Long_Algebraic_Notation_.28LAN.29
     *
     * LAN, but with moving piece prefix.
     *
     * eg. Bf1-c4
     */
    LongAlgebraicNotation,
    
    /**
     * https://www.chessprogramming.org/Algebraic_Chess_Notation#Standard_Algebraic_Notation_.28SAN.29
     * 
     * eg. Bc4
     */
    StandardAlgebraicNotation,
}
