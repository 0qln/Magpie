package Engine;

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
    private int _captured = Piece.WNone;

    // check squares, indexed by piece type
    private long[] _checks = new long[7];


    private BoardState() {}


    public int getPlys50() {
        return _plys50;
    }


    public int getPly() {
        return _ply;
    }


    public int getEpSquare() {
        return _epSquare;
    }


    public BitSet getCastling() {
        return _castling;
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
        private int _plys50 = 0;
        private int _ply = 0;
        private int _epSquare = -1;
        private BitSet _castling;
        private int _captured = Piece.WNone;
        // Used for computation on build
        private boolean _givesCheck = false;
        private Board _origin;

        public Builder(Board origin) {
            this._origin = origin;
            _castling = origin.getCastling();
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

        public Builder castling(Consumer<BitSet> castlingChanger) {
            castlingChanger.accept(this._castling);
            return this;
        }

        public Builder captured(int captured) {
            this._captured = captured;
            return this;
        }

        public BoardState build() {
            BoardState boardState = new BoardState();

            // User defined
            boardState._plys50  = this._plys50;
            boardState._ply = (this._ply);
            boardState._epSquare = (this._epSquare);
            boardState._castling = (this._castling);
            boardState._captured = (this._captured);

            // Auto Generated
            // TODO: generate these
            // PSEUDO:
            // boardState._checkers = _givesCheck ? (this._checkers) : 0;
            // boardState._blockers = (this._blockers);
            // boardState._pinners = (this._pinners);
            // boardState._checks = (this._checks);

            return boardState;
        }
    }
}
