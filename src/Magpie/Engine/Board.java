package Engine;

import static Engine.Utils.target;

import java.util.*;
import java.util.function.BiConsumer;

public class Board implements IBoard {
    // Bitboard array to store the color of the pieces
    private long[] _cBitboards = new long[2];

    // Bitboard array to store the types of the pieces
    private long[] _tBitboards = new long[7];

    // Piece array to store the pieces for each square
    private int[] _pieces = new int[64];

    // Counter for the pieces
    private int[] _pieceCount = new int[14];

    // The side the move
    private int _turn = Color.White;

    // A linked list to store the previous states of the game
    private LinkedList<BoardState> _stateStack = new LinkedList<BoardState>();

    // Game ply
    private int _ply;

    public long perft(int depth, BiConsumer<Short, Long> callback) {
        if (depth == 0) {
            return 1;
        }

        long moveC = 0, c;
        for (short move : MoveList.legal(this).getMoves()) {
            makeMove(move);
            moveC += (c = perft(depth - 1));
            callback.accept(move, c);
            undoMove(move);
        }

        return moveC;
    }

    public long perft(int depth) {
        if (depth == 0) {
            return 1;
        }

        long moveC = 0;
        for (short move : MoveList.legal(this).getMoves()) {
            makeMove(move);
            moveC += perft(depth - 1);
            undoMove(move);
        }

        return moveC;
    }

    public void makeMove(short move) {
        // Get context
        final int us = _turn, nus = Color.NOT(us);
        final int from = Move.getFrom(move);
        final int to = Move.getTo(move);
        final int flag = Move.getFlag(move);
        final int rNormF = from / 8 * 8;
        final int movingPiece = getPieceID(from);
        final int capturedPiece = flag == Move.EN_PASSANT_FLAG ? PieceUtil.create(PieceType.Pawn, nus) : getPieceID(to);

        // Increment game counters
        _turn = Color.NOT(_turn);
        _ply++;
        
        // Create new board state
        BoardState.Builder newState = new BoardState.Builder(this);
        newState
        // .comesWithCheck(false) // TODO
        .castling(_stateStack.getFirst().getCastling())
        .ply(_ply)
        .plys50(_ply);
        
        // Update castling rights
        // TODO: should be handled by state builder (?)
        if (PieceUtil.getType(movingPiece) == PieceType.King) {
            Castling.set(newState.getCastling(), us, false);
        }
        else if (PieceUtil.getType(movingPiece) == PieceType.Rook) {
            final int fromRank = Utils.rank(from);
            if ((us == Color.White ? 0 : 7) == fromRank) {
                final int fromFile = Utils.file(from);
                if (fromFile == Files.H) Castling.set(newState.getCastling(), Castling.KingSide, us, false);
                if (fromFile == Files.A) Castling.set(newState.getCastling(), Castling.QueenSide, us, false);
            }
        } 

        // Handle castling
        if (flag == Move.KING_CASTLE_FLAG) {
            movePiece(rNormF + Files.E, rNormF + Files.G);
            movePiece(rNormF + Files.H, rNormF + Files.F);
        } else if (flag == Move.QUEEN_CASTLE_FLAG) {
            movePiece(rNormF + Files.E, rNormF + Files.C);
            movePiece(rNormF + Files.A, rNormF + Files.D);
        }

        // Handle captures
        // Also get's activated on a promotion with capture, which is right.
        else if (PieceUtil.getType(capturedPiece) != PieceType.None) {
            int captureSquare = to;

            // Update castling if a rook has been captured
            Castling.update(newState.getCastling(), captureSquare, nus);

            // Handle en passant captures
            if (flag == Move.EN_PASSANT_FLAG) {
                captureSquare += (us * 2 - 1) * 8;
            }
            
            // Remove destination piece for captures
            removePiece(captureSquare);

            // Move the piece
            movePiece(from, to);

            // If a piece get's captured, the 50 move rule counter needs to be reset
            newState.plys50(0);
        }

        // Move The piece
        else {
            movePiece(from, to);
        }

        // Update state
        newState.captured(capturedPiece);

        // Pawns
        if (PieceUtil.getType(movingPiece) == PieceType.Pawn) {
            // Handle double pawn pushes
            if (flag == Move.DOUBLE_PAWN_PUSH_FLAG)
                newState.epSquare(to + (us * 2 - 1) * 8);

            // Handle promotions
            if (flag >= Move.PROMOTION_KNIGHT_FLAG && flag <= Move.CAPTURE_PROMOTION_QUEEN_FLAG) {
                int newPiece = PieceUtil.create(Move.getPromotion(move), us);
                removePiece(to);
                addPiece(to, newPiece);
            }

            // If a pawn get's moved, the 50 move rule counter needs to be reset
            newState.plys50(0);
        }

        // Update board state
        _stateStack.push(newState.build());
    }

    @Override
    public void undoMove(short move) {
        // Increment game counters
        _turn = Color.NOT(_turn);
        _ply--;

        // Get context
        int us = _turn;
        final int from = Move.getFrom(move);
        final int to = Move.getTo(move);
        final int flag = Move.getFlag(move);
        final int rNormF = from / 8 * 8;

        // Handle promotions
        if (Move.isPromotion(flag)) {
            // Replace the promotion with a pawn, the pawn will be moved later.
            removePiece(to);
            addPiece(to, PieceUtil.create(PieceType.Pawn, us));
        }

        // Handle castling
        if (flag == Move.KING_CASTLE_FLAG) {
            movePiece(rNormF + Files.G, rNormF + Files.E);
            movePiece(rNormF + Files.F, rNormF + Files.H);
        } else if (flag == Move.QUEEN_CASTLE_FLAG) {
            movePiece(rNormF + Files.C, rNormF + Files.E);
            movePiece(rNormF + Files.D, rNormF + Files.A);
        }

        // Move the piece
        else {
            movePiece(to, from);

            // Handle captures
            final int captured = _stateStack.getFirst().getCaptured();
            if (PieceUtil.getType(captured) != PieceType.None) {

                int captureSquare = to;

                // Handle en passant captures
                if (flag == Move.EN_PASSANT_FLAG) {
                    captureSquare += (us * 2 - 1) * 8;
                }

                addPiece(captureSquare, captured);
            }
        }

        // Pop board state from stack
        _stateStack.pop();
    }

    // TODO
    public boolean hasThreeFoldRepitition() {
        return false;
    }

    @Override
    public IMoveEncoder getMoveEncoder() {
        return move -> Move.toString(move);
    }

    @Override
    public IMoveDecoder getMoveDecoder() {
        return str -> {

            // For raw input moves
            if (str.charAt(0) == '@') {
                // e.g. @-14297
                return Short.parseShort(str.substring(1, str.length()));
            }

            int from = Misc.Utils.toSquareIndex(str.substring(0, 2));
            int to = Misc.Utils.toSquareIndex(str.substring(2, 4));

            // Use board context to determine the flag [e.g. castling, capturing, promotion]
            int movingPieceType = PieceUtil.getType(getPieceID(from));
            int destPieceType = PieceUtil.getType(getPieceID(to));
            int absDist = Math.abs(from - to);
            boolean captures = destPieceType == PieceType.None;
            int flag = captures ? Move.QUIET_MOVE_FLAG : Move.CAPTURE_FLAG;

            // Pawn extras
            if (movingPieceType == PieceType.Pawn) {
                // Double pawn push
                if (absDist == 16) {
                    flag = Move.DOUBLE_PAWN_PUSH_FLAG;
                }
                // En passant capture
                else if ((absDist == 7 || absDist == 9) && destPieceType == PieceType.None) {
                    flag = Move.EN_PASSANT_FLAG;
                }
                // Promotions
                else if (str.length() == 5) { // Is a promotion piece type specified?
                    int promotionType = PieceType.CMap.get(str.charAt(4));
                    flag = promotionType;

                    // Promotion captures
                    if (captures) {
                        flag += 4;
                    }
                }
            }
            // Castling
            else if (movingPieceType == PieceType.King && absDist == 2) {
                if (to % 8 == Files.G) {
                    flag = Move.KING_CASTLE_FLAG;
                } else if (to % 8 == Files.C) {
                    flag = Move.QUEEN_CASTLE_FLAG;
                }
            }

            return Move.create(from, to, flag);
        };
    }

    public int getCastlingCardinality() {
        return _stateStack.getFirst().getCastling().cardinality();
    }

    public BitSet getCastling() {
        return (BitSet) _stateStack.getFirst().getCastling().clone();
    }

    @Override
    public int getEnPassantSquare() {
        return _stateStack.getFirst().getEpSquare();
    }

    @Override
    public void addPiece(int square, int piece) {
        long bb = target(square);

        // add piece on type bitboard
        _tBitboards[PieceUtil.getType(piece)] |= bb;

        // add piece on color bitboard
        _cBitboards[PieceUtil.getColor(piece)] |= bb;

        // pieces
        _pieces[square] = piece;

        // increment piece count
        _pieceCount[piece]++;
    }

    @Override
    public void removePiece(int square) {
        int piece = getPieceID(square);
        long bb = target(square);

        // toggle piece on type bitboard
        _tBitboards[PieceUtil.getType(piece)] ^= bb;

        // toggle piece on color bitboard
        _cBitboards[PieceUtil.getColor(piece)] ^= bb;

        // pieces
        _pieces[square] = PieceUtil.None[0];

        // decrement piece count
        _pieceCount[piece]--;
    }

    @Override
    public int getPieceID(int square) {
        return _pieces[square];
    }

    private void movePiece(int from, int to) {
        int piece = _pieces[from];
        long fromTo = target(from) | target(to);
        _tBitboards[PieceUtil.getType(piece)] ^= fromTo;
        _cBitboards[PieceUtil.getColor(piece)] ^= fromTo;
        _pieces[from] = PieceUtil.None[0];
        _pieces[to] = piece;
    }

    @Override
    public int getTurn() {
        return _turn;
    }

    public String toString() {
        String result = "";
        for (int rank = 7; rank >= 0; rank--) {
            result += (rank + 1) + "  ";
            for (int file = 0; file <= 7; file++) {
                int square = Misc.Utils.sqaureIndex0(rank, file);
                int piece = getPieceID(square);
                char c = PieceUtil.toChar(piece);
                result += c + " ";
            }
            result += "\n";
        }
        result += "   a b c d e f g h\n";
        return result;
    }

    @Override
    public void setCastlingRights(int king, int white, boolean b) {
        _stateStack.getFirst().setCastlingRights(king, white, b);
    }

    public boolean canCastle(int side, int color) {
        return Castling.hasSpace(side, color, getOccupancy()) 
                && Castling.get(getCastling(), side, color);
    }

    @Override
    public void setEnpassant(int squareIndex) {
        _stateStack.getFirst().setEpSquare(squareIndex);
    }

    @Override
    public void setPlys50(int value) {
        _stateStack.getFirst().setPlys50(value);
    }

    @Override
    public void setTurn(int color) {
        _turn = color;
    }

    public long getBitboard(int pieceType, int color) {
        return _tBitboards[pieceType] & _cBitboards[color];
    }

    public long getCBitboard(int color) {
        return _cBitboards[color];
    }

    public long getTBitboard(int type) {
        return _tBitboards[type];
    }

    public long getOccupancy() {
        return getCBitboard(Color.White) | getCBitboard(Color.Black);
    }

    public long getNstmAttacks() {
        return _stateStack.getFirst().getNstmAttacks();
    }

    public long getCheckers() {
        return _stateStack.getFirst().getCheckers();
    }

    public long getBlockers() {
        return _stateStack.getFirst().getBlockers();
    }

    public boolean isInCheck() {
        return _stateStack.getFirst().getCheckers() != 0L;
    }

    public boolean isNotInCheck() {
        return Utils.countBits(_stateStack.getFirst().getCheckers()) == 0;
    }

    public boolean isInSingleCheck() {
        return Utils.countBits(_stateStack.getFirst().getCheckers()) == 1;
    }

    public boolean isInDoubleCheck() {
        return Utils.countBits(_stateStack.getFirst().getCheckers()) == 2;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Builder getBuilder() {
        return new Builder();
    }

    public static class Builder extends BoardBuilder<Board> {

        @NotRequired
        private String[] _fen = null;

        @Override
        protected Board _buildT() {
            if (_fen != null) {
                Board board1 = new Board();

                // Set up pieces
                int squareIdx = 63;
                for (int i = 0; i < _fen[0].length(); i++) {
                    // skip slashes
                    if (_fen[0].charAt(i) == '/') {
                        continue;
                    }

                    // handle digit
                    if (Character.isDigit(_fen[0].charAt(i))) {
                        squareIdx -= _fen[0].charAt(i) - '0';
                        continue;
                    }

                    // get piece char as Piece Type
                    int piece = PieceUtil.fromChar(_fen[0].charAt(i));

                    // evaluate
                    board1.addPiece(squareIdx ^ 7, piece);

                    squareIdx--;
                }

                // turn
                board1.setTurn(_fen[1].contains("w")
                        ? Color.White
                        : Color.Black);

                BoardState.Builder stateBuilder = new BoardState.Builder(board1);
                stateBuilder
                        // castling
                        .castling(Castling.create(
                                _fen[2].contains("K"),
                                _fen[2].contains("Q"),
                                _fen[2].contains("k"),
                                _fen[2].contains("q")))

                        // en passant
                        .epSquare(!_fen[3].contains("-")
                                ? Misc.Utils.toSquareIndex(_fen[3])
                                : -1)

                        // plys for 50 move rule
                        .plys50(_fen.length > 4 ? Integer.parseInt(_fen[4]) : 0)
                        .ply(0);

                board1._stateStack.push(stateBuilder.build());

                return board1;
            } else {
                Board board = new Board();

                board._stateStack.push(
                        new BoardState.Builder(board)
                                .castling(Castling.empty())
                                .ply(0)
                                .plys50(0)
                                .build());

                return board;
            }
        }

        @Override
        public Builder fen(String[] fen) {
            _fen = fen;
            return this;
        }

        public Builder fen(String fen) {
            _fen = fen.split(" ");
            return this;
        }
    }
}
