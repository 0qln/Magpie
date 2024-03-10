package Engine;

import static Engine.Utils.*;

import java.util.BitSet;

public class BoardState {

    private long _checkers, _blockers;
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
    private int _captured = None.ID_White;

    // Zobrist key for the position
    private long _key;
    private boolean _hasThreeFoldRepitition;

    public BoardState(
            long _checkers,
            long _blockers,
            int _plys50,
            int _ply,
            int _epSquare,
            BitSet _castling,
            int _captured,
            long _nstmAttacks,
            long _key,
            boolean _hasThreeFoldRepitition) {
        this._checkers = _checkers;
        this._blockers = _blockers;
        this._plys50 = _plys50;
        this._ply = _ply;
        this._epSquare = _epSquare;
        this._castling = _castling;
        this._captured = _captured;
        this._nstmAttacks = _nstmAttacks;
        this._key = _key;
        this._hasThreeFoldRepitition = _hasThreeFoldRepitition;
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

    public long getBlockers() {
        return _blockers;
    }

    public long getKey() {
        return _key;
    }

    public boolean hasThreeFoldRepitition() {
        return _hasThreeFoldRepitition;
    }

    public static class Builder extends Misc.Builder<BoardState> {

        @Required
        private Integer _plys50;
        @Required
        private Integer _ply;
        @NotRequired
        private int _epSquare = -1;
        @Required
        private BitSet _castling;
        @NotRequired
        private int _captured = None.ID_White;
        @Required
        private Board _origin;
        @Required
        private Long _key;
        @Required
        private Boolean _hasThreeFoldRepitition;

        public Builder(Board origin) {
            this._origin = origin;
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

        public int getEpSquare() {
            return this._epSquare;
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

        public Builder setCastlingRights(int kingside, int color, boolean value) {
            _castling.set(color << 1 | kingside, value);
            return this;
        }

        public Builder captured(int captured) {
            this._captured = captured;
            return this;
        }

        public Builder initKey(long key) {
            this._key = key;
            return this;
        }

        public Builder updateKey(long update) {
            this._key ^= update;
            return this;
        }

        public long getKey() {
            return this._key;
        }

        public Builder threeFoldRepitition(boolean value) {
            this._hasThreeFoldRepitition = value;
            return this;
        }

        @Override
        protected BoardState _buildT() {
            long checkers = 0, blockers = 0, nstmAttacks = 0;

            final int us = _origin.getTurn(), nus = Color.NOT(us);
            final long enemiesBB = _origin.getCBitboard(nus);
            long[] enemies = { enemiesBB };
            final long kingBB = _origin.getBitboard(King.ID_Type, us);
            final int kingSq = lsb(kingBB);
            while (enemies[0] != 0) {
                final int enemySquare = popLsb(enemies);
                final long pieces = _origin.getOccupancy();
                final int enemyID = _origin.getPieceID(enemySquare);
                final long attacks = PieceType
                    .fromID(enemyID)
                    .getGenerator()
                    .attacks(enemySquare, pieces, nus);

                if ((attacks & kingBB) != 0) 
                    checkers |= target(enemySquare);                    
                

                nstmAttacks |= attacks;
            }

            long[] xRayCheckers = { 
                (
                    Rook.generator.attacks(kingSq) & (_origin.getTBitboard(Rook.ID_Type) | _origin.getTBitboard(Queen.ID_Type)) |
                    Bishop.generator.attacks(kingSq) & (_origin.getTBitboard(Bishop.ID_Type) | _origin.getTBitboard(Queen.ID_Type))
                ) 
                & _origin.getCBitboard(Color.NOT(us)) 
            };

            while (xRayCheckers[0] != 0) {
                int xRayChecker = popLsb(xRayCheckers);
                long betweenSquaresBB = Masks.squaresBetween(kingSq, xRayChecker);
                long piecesBetweenSniperAndKingBB = _origin.getOccupancy() & betweenSquaresBB;

                if (countBits(piecesBetweenSniperAndKingBB) == 1) 
                    blockers |= piecesBetweenSniperAndKingBB;
            }

            return new BoardState(
                    checkers,
                    blockers,
                    this._plys50,
                    this._ply,
                    this._epSquare,
                    this._castling,
                    this._captured,
                    nstmAttacks,
                    this._key,
                    this._hasThreeFoldRepitition);
        }
    }
}
