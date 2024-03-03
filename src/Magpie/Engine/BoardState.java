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
    private int _captured = PieceUtil.None[0];

    public BoardState(
            long _checkers,
            long _blockers,
            int _plys50,
            int _ply,
            int _epSquare,
            BitSet _castling,
            int _captured,
            long _nstmAttacks) {
        this._checkers = _checkers;
        this._blockers = _blockers;
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

    public long getBlockers() {
        return _blockers;
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
        private int _captured = PieceUtil.None[0];
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
            long checkers = 0, blockers = 0, nstmAttacks = 0, pinners = 0;

            final int us = _origin.getTurn();
            final long enemiesBB = _origin.getCBitboard(Color.NOT(us));
            long[] enemies = { enemiesBB };
            final long kingBB = _origin.getBitboard(PieceType.King, us);
            final int kingSq = lsb(kingBB);
            while (enemies[0] != 0) {
                final int enemySquare = popLsb(enemies);
                final long pieces = _origin.getOccupancy();
                final int enemyID = _origin.getPieceID(enemySquare);
                final long attacks = Piece
                    .fromID(enemyID)
                    .getGenerator()
                    .attacks(enemySquare, pieces);

                if ((attacks & kingBB) != 0) 
                    checkers |= target(enemySquare);                    
                

                nstmAttacks |= attacks;
            }

            // System.out.println(_origin.toString());

            long[] xRayCheckers = { 
                (
                    Rook.generator.attacks(kingSq) & (_origin.getTBitboard(PieceType.Rook) | _origin.getTBitboard(PieceType.Queen)) |
                    Bishop.generator.attacks(kingSq) & (_origin.getTBitboard(PieceType.Bishop) | _origin.getTBitboard(PieceType.Queen))
                ) 
                & _origin.getCBitboard(Color.NOT(us)) 
            };

            // printBB(xRayCheckers);

            while (xRayCheckers[0] != 0) {
                int xRayChecker = popLsb(xRayCheckers);
                long betweenSquaresBB = Masks.squaresBetween(kingSq, xRayChecker);
                long piecesBetweenSniperAndKingBB = _origin.getOccupancy() & betweenSquaresBB;

                // printBB(betweenSquaresBB);

                if (countBits(piecesBetweenSniperAndKingBB) == 1) {
                    blockers |= piecesBetweenSniperAndKingBB;
                    pinners |= target(xRayChecker);
                }
            }

            // printBB(pinners);
            // printBB(blockers);
            // printBB(checkers);
            // printBB(nstmAttacks);

            return new BoardState(
                    checkers,
                    blockers,
                    this._plys50,
                    this._ply,
                    this._epSquare,
                    this._castling,
                    this._captured,
                    nstmAttacks);
        }
    }
}
