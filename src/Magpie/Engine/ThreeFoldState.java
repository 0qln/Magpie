package Engine;

import java.util.Arrays;

/*
 * https://en.wikipedia.org/wiki/Threefold_repetition
 * 
 * TODO: https://www.chessprogramming.org/Zobrist_Hashing
 */
public class ThreeFoldState {
    public final int stm;
    public final byte[] pieces = new byte[64];
    public final byte castlingCardinality;
    public final byte ep;

    public ThreeFoldState(Board board) {
        stm = board.getTurn();
        for (int i = 0; i < pieces.length; i++) {
            pieces[i] = (byte)board.getPiece(i);
        }
        castlingCardinality = (byte)board.getCastlingCardinality();
        ep = (byte)board.getEnPassantSquare();
    }

    @Override
    public boolean equals(Object obj) {
        ThreeFoldState other;
        if (obj instanceof ThreeFoldState) {
            other = (ThreeFoldState)obj;
        }
        else {
            return false;
        }

        return (
            this.stm == other.stm &&
            this.ep == other.ep &&
            this.castlingCardinality == other.castlingCardinality &&
            Arrays.equals(this.pieces, other.pieces)
        );
    }

    // TODO
    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
