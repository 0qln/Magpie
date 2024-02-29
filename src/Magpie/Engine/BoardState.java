package Engine;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.BitSet;
import java.util.function.Consumer;

public class BoardState
{
   
    // Plys until 50 move rule
    private int _plys50 = 0;

    // Game ply
    private int _ply = 0;

    // The square where an en passant capture is possible, if non are possible -1
    private int _epSquare = -1;

    // Which side can castle where?
    private BitSet _castling = new BitSet(4);

    // checkers 
    private long _checkers = 0;

    // check blockers, indexed by color
    private long[] _blockers = new long[2];
    
    // pinners, indexed by color
    private long[] _pinners = new long[2];

    // captured piece, the piece that was captured when this state was reached
    private int _captured = Piece.None[0];

    // check squares, indexed by piece type
    private long[] _checks = new long[7];


    private BoardState() {}


    public int getPlys50() {
        return _plys50;
    }
    public void setPlys50(int value) {
        _plys50 = value;
    }

    public int getPly() {
        return _ply;
    }


    public int getEpSquare() {
        return _epSquare;
    }
    public void setEpSquare(int value) {
        _epSquare = value;
    }


    public BitSet getCastling() {
        return _castling;
    }

    /**
     * @param kingside [ 1 | 0 ] 
     * @param color 
     * @param value
     */
    public void setCastlingRights(int kingside, int color, boolean value) {
        _castling.set(color << 1 | kingside, value);
    }

    public long getCheckers() {
        return _checkers;
    }


    public long[] getBlockers() {
        return _blockers;
    }


    public long[] getPinners() {
        return _pinners;
    }


    public int getCaptured() {
        return _captured;
    }


    public long[] getChecks() {
        return _checks;
    }


    public static class Builder {
       
        // Cache the target values
        @BuilderRequired private Integer _plys50;
        @BuilderRequired private Integer _ply;
        @BuilderNotRequired private int _epSquare = -1;
        @BuilderRequired private BitSet _castling;
        @BuilderNotRequired private int _captured = Piece.None[0];
        // Dynamically used for computation on build
        // TODO: make required, when it is used in later deployment
        @BuilderNotRequired private boolean _givesCheck = false;
        @BuilderRequired private Board _origin;


        public Builder(Board origin) {
            this._origin = origin;
        }

        public Builder givesCheck(boolean does) {
            _givesCheck = does;
            return this;
        }

        public Builder plys50(int plys50) {
            this._plys50 = plys50;
            return this;
        }

        public Builder ply(int ply) {
            this._ply = ply;
            return this;
        }

        public Builder epSquare(int epSquare) {
            this._epSquare = epSquare;
            return this;
        }

        public Builder castling(byte[] set) {
            _castling = BitSet.valueOf(set);
            return this;
        }

        public BitSet getCastling() {
            return _castling;
        }

        public Builder captured(int captured) {
            this._captured = captured;
            return this;
        }

        public BoardState buildUnchecked() {
            BoardState boardState = new BoardState();

            // User defined
            boardState._plys50 = this._plys50;
            boardState._ply = (this._ply);
            boardState._epSquare = (this._epSquare);
            boardState._castling = (this._castling);
            boardState._captured = (this._captured);

            // Auto Generated
            // TODO: generate these
            // boardState._checkers = _givesCheck ? (this._checkers) : 0;
            // boardState._blockers = (this._blockers);
            // boardState._pinners = (this._pinners);
            // boardState._checks = (this._checks);

            return boardState;

        }

        public BoardState build() throws IllegalArgumentException, IllegalAccessException {
            BoardState boardState = new BoardState();

            // Check if required fields are set
            // TODO: reflection is slow, so maybe remove this to speed up in the release build
            for (Field field : this.getClass().getDeclaredFields()) {
                if (field.isAnnotationPresent(BuilderRequired.class) && field.get(this) == null) {
                    throw new IllegalStateException("Required field not set: " + field.getName());
                }
            }

            // User defined
            boardState._plys50 = this._plys50;
            boardState._ply = (this._ply);
            boardState._epSquare = (this._epSquare);
            boardState._castling = (this._castling);
            boardState._captured = (this._captured);

            // Auto Generated
            // TODO: generate these
            // boardState._checkers = _givesCheck ? (this._checkers) : 0;
            // boardState._blockers = (this._blockers);
            // boardState._pinners = (this._pinners);
            // boardState._checks = (this._checks);

            return boardState;
        }
    }


    
}
