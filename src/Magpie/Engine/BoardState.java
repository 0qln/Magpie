package Engine;

import static Engine.Utils.popLsb;
import static Engine.Utils.printBB;
import static Engine.Utils.target;

import java.util.BitSet;

public class BoardState {

    /*
     * Keep track of:
     * 
     * 1. The pieces that are checking
     * 1.0 (As bitboard)
     * 1.1 -> Double check and single check == bitcnt
     * 
     * 2. The squares that can block a check
     * 2.0 (As bitboard)
     * 
     * 3. Pins
     * 3.0 (As bitboard)
     * 3.1 -> XRay attacks
     * 3.2 Including the square that the pinning piece is standing on
     * 
     */
    private long _checkers, _blockers, _pins;
    private long _nstmAttacks;

    // Plys until 50 move rule
    private int _plys50 = 0;

    // Game ply
    private int _ply = 0;

    // The square where an en passant capture is possible, if non are possible -1
    private int _epSquare = -1;

    // Which side can castle where?
    // [ 00 ->  ]
    private BitSet _castling = new BitSet(4);

    // captured piece, the piece that was captured when this state was reached
    private int _captured = Piece.None[0];

    public BoardState(
            long _checkers,
            long _blockers,
            long _pins,
            int _plys50,
            int _ply,
            int _epSquare,
            BitSet _castling,
            int _captured,
            long _nstmAttacks) {
        this._checkers = _checkers;
        this._blockers = _blockers;
        this._pins = _pins;
        this._plys50 = _plys50;
        this._ply = _ply;
        this._epSquare = _epSquare;
        this._castling = _castling;
        this._captured = _captured;
        this._nstmAttacks = _nstmAttacks;
    }

    public long getCheckers() {
        return _checkers;
    }

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

    public void setCastlingRights(int kingside, int color, boolean value) {
        _castling.set(color << 1 | kingside, value);
    }

    public int getCaptured() {
        return _captured;
    }

    public long getNstmAttacks() {
        return _nstmAttacks;
    }

    public static class Builder extends Misc.Builder<BoardState> {

        // Cache the target values
        @Required
        private Integer _plys50;
        @Required
        private Integer _ply;
        @NotRequired
        private int _epSquare = -1;
        @Required
        private BitSet _castling;
        @NotRequired
        private int _captured = Piece.None[0];
        // Dynamically used for computation on build
        // @BuilderRequired
        // private boolean _comesWithCheck = false;
        @Required
        private Board _origin;

        public Builder(Board origin) {
            this._origin = origin;
        }

        // public Builder comesWithCheck(boolean does) {
        // _comesWithCheck = does;
        // return this;
        // }

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

        public Builder castling(BitSet set) {
            _castling = (BitSet)set.clone();
            return this;
        }

        public BitSet getCastling() {
            return _castling;
        }

        public void setCastlingRights(int kingside, int color, boolean value) {
            _castling.set(color << 1 | kingside, value);
        }

        public Builder captured(int captured) {
            this._captured = captured;
            return this;
        }

        @Override
        protected BoardState _buildT() {
            long checkers = 0, blockers = ~0L, pins = 0, nstmAttacks = 0;

            final long[] enemies = { _origin.getCBitboard(Color.NOT(_origin.getTurn())) };
            final long king = _origin.getBitboard(PieceType.King, _origin.getTurn());
            // printBB(king);
            while (enemies[0] != 0) {
                final int enemy = popLsb(enemies);
                final long pieces = _origin.getOccupancy();
                final long attacks;
                switch (Piece.getType(_origin.getPiece(enemy))) {
                    case PieceType.Pawn: attacks = PawnMoveGenerator.attacks(enemy); break;
                    case PieceType.Knight: attacks = KnightMoveGenerator.attacks(enemy); break;
                    case PieceType.Bishop: attacks = BishopMoveGenerator.attacks(enemy, pieces); break;
                    case PieceType.Rook: attacks = RookMoveGenerator.attacks(enemy, pieces); break;
                    case PieceType.Queen: attacks = QueenMoveGenerator.attacks(enemy, pieces); break;
                    case PieceType.King: attacks = KingMoveGenerator.attacks(enemy); break;
                    default: continue;
                }

                if ((attacks & king) != 0) 
                    checkers |= target(enemy);                    
                

                nstmAttacks |= attacks;
            }

            // printBB(checkers);
            // printBB(nstmAttacks);

            return new BoardState(
                    checkers,
                    blockers,
                    pins,
                    this._plys50,
                    this._ply,
                    this._epSquare,
                    this._castling,
                    this._captured,
                    nstmAttacks);
        }
    }
}
