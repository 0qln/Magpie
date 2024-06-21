package Engine;

import static Engine.Utils.countBits;
import static Engine.Utils.deactivateBit;
import static Engine.Utils.file;
import static Engine.Utils.lsb;
import static Engine.Utils.popLsb;
import static Engine.Utils.target;

import java.util.*;
import java.util.function.BiConsumer;

public class Board implements IBoard {
    
    private Board board = this;

    // Keep track of positions for three fold repitition.
    byte[] _positions = new byte[Misc.Utils.mb(30)];
    // When incrementing/decrementing, we should not need to check for overflows as 
    // the search will abort if the position occured more than thrice (three fold repitition).
    // In case that the search does not abort (ie. perft) we don't need to check 
    // for overflows, as the occurance of the position should not be relevant for such searches.
    private void positionsDecr(long key) {
        _positions[(int)Math.abs(key % _positions.length)]--;
    }
    private byte posOccurances(long key) {
        return _positions[(int)Math.abs(key % _positions.length)];
    }
    private void positionsIncr(long key) {
        _positions[(int)Math.abs(key % _positions.length)]++;
    }

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

    public long perft(int depth, BiConsumer<Short, Long> callback, SearchLimit limit) {
        if (depth == 0) {
            return 1;
        }

        long moveC = 0, c;
        for (short move : MoveList.legal(this, limit.capturesOnly).getMoves()) {
            makeMove(move);
            moveC += (c = perft(depth - 1, limit));
            callback.accept(move, c);
            undoMove(move);
        }

        return moveC;
    }

    public long perft(int depth, SearchLimit limit) {
        if (depth == 0) {
            return 1;
        }

        long moveC = 0;
        for (short move : MoveList.legal(this, limit.capturesOnly).getMoves()) {
            makeMove(move);
            moveC += perft(depth - 1, limit);
            undoMove(move);
        }

        return moveC;
    }

    // Inlining the calls to BoardState.Builder.updateKey shows a non trivial gain in performance.
    public void makeMove(short move) {
        // Get context
        final int us = _turn, nus = Color.NOT(us);
        final int from = Move.getFrom(move);
        final int to = Move.getTo(move);
        final int flag = Move.getFlag(move);
        final int rNormF = from / 8 * 8;
        final int movingPiece = getPieceID(from);
        final int capturedPiece = flag == Move.EN_PASSANT_FLAG ? Piece.create(Pawn.ID_Type, nus) : getPieceID(to);
        final BitSet oldCastling = getCastling();
        long key = getKey() ^ Zobrist.stm;

        // Increment game counters
        _turn = Color.NOT(_turn);
        _ply++;

        // Create new board state
        BoardState.Builder newState = new BoardState.Builder(this);
        newState
                .castling(oldCastling)
                .ply(_ply)
                .plys50(_stateStack.getFirst().getPlys50() + 1)
                // .initKey(this.getKey())
                // The xor operation is it's own inverse, thus we just apply the operation on
                // every new move, regardless of the current stm, becuase it will inverse, the
                // black color if the current stm is white.
                // .updateKey(Zobrist.stm)
                ;

        // Handle castling rights
        // TODO: should be handled by state builder (?)
        if (Piece.getType(movingPiece) == King.ID_Type) {
            Castling.set(newState.getCastling(), us, false);
        } else if (Piece.getType(movingPiece) == Rook.ID_Type) {
            final int fromRank = Utils.rank(from);
            if ((us == Color.White ? 0 : 7) == fromRank) {
                final int fromFile = Utils.file(from);
                if (fromFile == Files.H)
                    Castling.set(newState.getCastling(), Castling.KingSide, us, false);
                if (fromFile == Files.A)
                    Castling.set(newState.getCastling(), Castling.QueenSide, us, false);
            }
        }

        // Handle castling => Also move rook, king will be moved later anyways
        if (flag == Move.KING_CASTLE_FLAG) {
            final int rfrom = rNormF + Files.H, rto = rNormF + Files.F;
            // (In the hash)
            key ^=Zobrist.pieceSQ[_pieces[rfrom]][rfrom] ^Zobrist.pieceSQ[_pieces[rfrom]][rto];
            // newState.updateKey(Zobrist.pieceSQ[getPieceID(rfrom)][rfrom])
            //         .updateKey(Zobrist.pieceSQ[getPieceID(rfrom)][rto]);
            // (On the board)
            movePiece(rfrom, rto);
        } else if (flag == Move.QUEEN_CASTLE_FLAG) {
            final int rfrom = rNormF + Files.A, rto = rNormF + Files.D;
            // (In the hash)
            key ^=Zobrist.pieceSQ[_pieces[rfrom]][rfrom] ^Zobrist.pieceSQ[_pieces[rfrom]][rto];
            // newState.updateKey(Zobrist.pieceSQ[getPieceID(rfrom)][rfrom])
            //         .updateKey(Zobrist.pieceSQ[getPieceID(rfrom)][rto]);
            // (On the board)
            movePiece(rfrom, rto);
        }

        // Handle captures
        // Also get's activated on a promotion with capture, which is right.
        else if (Piece.getType(capturedPiece) != None.ID_Type) {
            int captureSquare = to;

            // Update castling if a rook has been captured
            Castling.update(newState.getCastling(), captureSquare, nus);

            // Handle en passant captures
            if (flag == Move.EN_PASSANT_FLAG)
                captureSquare += (us * 2 - 1) * 8;

            // Remove destination piece for captures
            removePiece(captureSquare);

            // If a piece get's captured, the 50 move rule counter needs to be reset
            newState.plys50(0);

            // Update key
            // newState.updateKey(Zobrist.pieceSQ[capturedPiece][captureSquare]);
            key ^= Zobrist.pieceSQ[capturedPiece][captureSquare];
        }

        // Move The piece
        movePiece(from, to);

        newState
                // Remember captured piece
                .captured(capturedPiece)
                // In hash, remove piece from old position
                // .updateKey(Zobrist.pieceSQ[movingPiece][from])
                // In hash, place piece at new position
                // .updateKey(Zobrist.pieceSQ[movingPiece][to])
                // In hash, reset en passant file
                // .updateKey(getEnPassantSquare() != -1
                //         ? Zobrist.enPassant[file(getEnPassantSquare())]
                //         : 0)
                        ;
                    
        key ^= Zobrist.pieceSQ[movingPiece][to] ^ Zobrist.pieceSQ[movingPiece][from];

        if (getEnPassantSquare() != -1)
            key ^= Zobrist.enPassant[file(getEnPassantSquare())];

        // Pawns
        if (Piece.getType(movingPiece) == Pawn.ID_Type) {
            // Handle double pawn pushes
            if (flag == Move.DOUBLE_PAWN_PUSH_FLAG) {
                newState.epSquare(to + (us * 2 - 1) * 8)
                        // .updateKey(Zobrist.enPassant[file(to)])
                        ;
                key ^= Zobrist.enPassant[file(to)];
            }

            // Handle promotions
            if (flag >= Move.PROMOTION_KNIGHT_FLAG && flag <= Move.CAPTURE_PROMOTION_QUEEN_FLAG) {
                int newPiece = Piece.create(Move.getPromotion(move), us);
                removePiece(to);
                addPiece(to, newPiece);
                // In hash, remove pawn
                // newState.updateKey(Zobrist.pieceSQ[movingPiece][to])
                //         // Add promotion
                //         .updateKey(Zobrist.pieceSQ[newPiece][to]);
                key ^= Zobrist.pieceSQ[movingPiece][to] ^ Zobrist.pieceSQ[newPiece][to];
            }

            // If a pawn get's moved, the 50 move rule counter needs to be reset
            newState.plys50(0);
        }

        // In hash, update castling, if it has changed
        if (!oldCastling.equals(newState.getCastling())) {
            // Remove old castling rights
            // newState.updateKey(Zobrist.castling[Castling.Key(oldCastling)])
            //         // Add new castling rights
            //         .updateKey(Zobrist.castling[Castling.Key(newState.getCastling())]);

            key ^= Zobrist.castling[Castling.Key(oldCastling)] ^ Zobrist.castling[Castling.Key(newState.getCastling())];
        }

        // Remember new position
        positionsIncr(key);

        // Three fold repitition
        newState.threeFoldRepitition(posOccurances(key) >= 3);

        // Initiate key for new board state
        newState.initKey(key);

        // Update board state
        _stateStack.push(newState.build());
    }

    @Override
    public void undoMove(short move) {
        // Forget latest position
        positionsDecr(getKey());

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
            addPiece(to, Piece.create(Pawn.ID_Type, us));
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
            if (Piece.getType(captured) != None.ID_Type) {

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

    public boolean hasThreeFoldRepitition() {
        return _stateStack.getFirst().hasThreeFoldRepitition();
    }

    @Override
    public IMoveEncoder getMoveEncoder() {
        return move -> Move.toString(move);
    }
    
    // UNTESTED
    class MoveDecoder_LongAlgebraicNotation implements IMoveDecoder {
        @Override
        public short decode(String move) {
            // TODO: 
            boolean isPieceMove = move.length() == 6;
            int indexShift = isPieceMove ? 1 : 0; // pawns are not included in long algebraic notation
            int from = Misc.Utils.toSquareIndex(move.substring(0 + indexShift, 2 + indexShift));
            int to = Misc.Utils.toSquareIndex(move.substring(3 + indexShift, 4 + indexShift));

            // Use board context to determine the flag [e.g. castling, capturing, promotion]
            int movingPieceType = Piece.getType(getPieceID(from)); // TODO: we can get the moving piece type from the move notation
            int destPieceType = Piece.getType(getPieceID(to));
            int absDist = Math.abs(from - to);
            boolean captures = destPieceType == None.ID_Type;
            int flag = captures ? Move.QUIET_MOVE_FLAG : Move.CAPTURE_FLAG;

            // Pawn extras
            if (movingPieceType == Pawn.ID_Type) {
                // Double pawn push
                if (absDist == 16) {
                    flag = Move.DOUBLE_PAWN_PUSH_FLAG;
                }
                // En passant capture
                else if ((absDist == 7 || absDist == 9) && destPieceType == None.ID_Type) {
                    flag = Move.EN_PASSANT_FLAG;
                }
                // Promotions
                else if (move.length() == 5) { // Is a promotion piece type specified?
                    int promotionType = PieceType.CMap.get(move.charAt(4));
                    flag = promotionType;

                    // Promotion captures
                    if (captures) {
                        flag += 4;
                    }
                }
            }
            // Castling
            else if (movingPieceType == King.ID_Type && absDist == 2) {
                if (to % 8 == Files.G) {
                    flag = Move.KING_CASTLE_FLAG;
                }
                else if (to % 8 == Files.C) {
                    flag = Move.QUEEN_CASTLE_FLAG;
                }
            }

            return Move.create(from, to, flag);
        }
    }
    
    class MoveDecoder_LongAlgebraicNotation_UCI implements IMoveDecoder {
        @Override
        public short decode(String move) {
            int from = Misc.Utils.toSquareIndex(move.substring(0, 2));
            int to = Misc.Utils.toSquareIndex(move.substring(2, 4));

            // Use board context to determine the flag [e.g. castling, capturing, promotion]
            int movingPieceType = Piece.getType(getPieceID(from));
            int destPieceType = Piece.getType(getPieceID(to));
            int absDist = Math.abs(from - to);
            boolean captures = destPieceType == None.ID_Type;
            int flag = captures ? Move.QUIET_MOVE_FLAG : Move.CAPTURE_FLAG;

            // Pawn extras
            if (movingPieceType == Pawn.ID_Type) {
                // Double pawn push
                if (absDist == 16) {
                    flag = Move.DOUBLE_PAWN_PUSH_FLAG;
                }
                // En passant capture
                else if ((absDist == 7 || absDist == 9) && destPieceType == None.ID_Type) {
                    flag = Move.EN_PASSANT_FLAG;
                }
                // Promotions
                else if (move.length() == 5) { // Is a promotion piece type specified?
                    int promotionType = PieceType.CMap.get(move.charAt(4));
                    flag = promotionType;

                    // Promotion captures
                    if (captures) {
                        flag += 4;
                    }
                }
            }
            // Castling
            else if (movingPieceType == King.ID_Type && absDist == 2) {
                if (to % 8 == Files.G) {
                    flag = Move.KING_CASTLE_FLAG;
                }
                else if (to % 8 == Files.C) {
                    flag = Move.QUEEN_CASTLE_FLAG;
                }
            }

            return Move.create(from, to, flag);
        }
    }
    
    class MoveDecoder_StandardAlgebraicNotation implements IMoveDecoder {
        @Override
        public short decode(String move) {
            
            // '0' is FIDE standard and 'O' is PGN standard.
            if (move.equals("O-O-O") || move.equals("0-0-0")) {
                if (getTurn() == Color.White) {
                    return Move.create(
                        Misc.Utils.sqaureIndex0(0, Files.E), 
                        Misc.Utils.sqaureIndex0(0, Files.C), 
                        Move.QUEEN_CASTLE_FLAG);
                }
                else {
                    return Move.create(
                        Misc.Utils.sqaureIndex0(7, Files.E), 
                        Misc.Utils.sqaureIndex0(7, Files.C), 
                        Move.QUEEN_CASTLE_FLAG);
                }
            }
            else if (move.equals("O-O") || move.equals("0-0")) {
                if (getTurn() == Color.White) {
                    return Move.create(
                        Misc.Utils.sqaureIndex0(0, Files.E), 
                        Misc.Utils.sqaureIndex0(0, Files.G), 
                        Move.KING_CASTLE_FLAG);
                }
                else {
                    return Move.create(
                        Misc.Utils.sqaureIndex0(7, Files.E), 
                        Misc.Utils.sqaureIndex0(7, Files.G), 
                        Move.KING_CASTLE_FLAG);
                }
            }
            
            // Check / CheckMate hints can be ignored.
            move = move.replace("#", "");
            move = move.replace("+", "");

            int from, to, flag = Move.QUIET_MOVE_FLAG;
            boolean captures = false, promotes = false;
            
            promotes = move.charAt(move.length() - 2) == '=';

            int endOff = promotes ? 2 : 0;

            if (move.length() >= (3 + endOff))
                captures = move.charAt(move.length() - (3 + endOff)) == 'x';
            
            if (captures) 
                flag = Move.CAPTURE_FLAG;

            to = Misc.Utils.toSquareIndex(move.substring(
                move.length() - (2 + endOff), 
                move.length() - endOff)); 

            int destPieceID = getPieceID(to);
            int movingPieceTypeID = Character.isLowerCase(move.charAt(0)) 
                ? Pawn.ID_Type 
                : PieceType.CMap.get(Character.toLowerCase(move.charAt(0)));
            int bgnOff = movingPieceTypeID == Pawn.ID_Type ? 0 : 1;
            PieceType.MoveGenerator generator = PieceType.fromTypeID(movingPieceTypeID).getGenerator();

            // Filter out illegal moves. After this step, possibleOriginSquares contains only squares that can
            // be legal origin squares for a piece of type movingPieceTypeID that can reach the to square.
            long originSquares = 0;
            long[] possibleOriginSquares = { getBitboard(movingPieceTypeID, getTurn()) };
            while (possibleOriginSquares[0] != 0) {
                int possibleOriginSquareIdx = popLsb(possibleOriginSquares);
                long possibleOriginSquare = target(possibleOriginSquareIdx);
                // Illegal moves are not considered in amgiuous moves
                MoveList legalMovesFromOrigin = MoveList.legal(board, captures, generator);
                for (short m : legalMovesFromOrigin.getMoves()) {
                    if (Move.getFrom(m) != possibleOriginSquareIdx)
                        continue;
                    if (Move.getTo(m) == to) {
                        originSquares ^= possibleOriginSquare;
                        break;
                    }
                }
            }
            
            // Read and interpret the file/rank disambiguation.
            int originSquareCount = countBits(originSquares);
            if (originSquareCount <= 1) {
                from = lsb(originSquares);
            }
            else {
                // Use disambiguation to determine the origin square.
                char fromHint1 = move.charAt(bgnOff);
                if (Character.isAlphabetic(fromHint1)) {
                    // hint 1 is file
                    originSquares &= Masks.Files[fromHint1 - 'a'];
                    if (Character.isDigit(move.charAt(bgnOff + 1))) {
                        char fromHint2 = move.charAt(bgnOff + 1);
                        // hint 2 is rank ( => square )
                        originSquares &= Masks.Ranks[fromHint2 - '1'];
                    }
                }
                else {
                    // hint 1 is rank
                    originSquares &= Masks.Ranks[fromHint1 - '1'];
                }
                
                from = lsb(originSquares);
            }

            int absDist = Math.abs(from - to);
            
            // Pawn extras
            if (movingPieceTypeID == Pawn.ID_Type) {
                // Double pawn push
                if (absDist == 16) {
                    flag = Move.DOUBLE_PAWN_PUSH_FLAG;
                }
                // En passant capture
                else if ((absDist == 7 || absDist == 9) && Piece.getType(destPieceID) == None.ID_Type) {
                    flag = Move.EN_PASSANT_FLAG;
                }
                // Promotions
                else if (promotes) {
                    char promotion = Character.toLowerCase(move.charAt(move.length() - 1));
                    int promotionPieceType = PieceType.CMap.get(promotion);
                    flag = promotionPieceType;

                    if (captures)
                        flag += 4;
                }
            }
            
            return Move.create(from, to, flag);
        }
    }
    
    
    @Override
    public IMoveDecoder getMoveDecoder(MoveFormat format) {
        switch (format) {
            case RawDec:
                return new MoveDecoder_Raw();
            case LongAlgebraicNotation_UCI:
                return new MoveDecoder_LongAlgebraicNotation_UCI();
            case LongAlgebraicNotation:
                return new MoveDecoder_LongAlgebraicNotation();
            case StandardAlgebraicNotation:
                return new MoveDecoder_StandardAlgebraicNotation();
            case ICCF: case Smith: default:
                throw new UnsupportedOperationException();
        }
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
        _tBitboards[Piece.getType(piece)] |= bb;

        // add piece on color bitboard
        _cBitboards[Piece.getColor(piece)] |= bb;

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
        _tBitboards[Piece.getType(piece)] ^= bb;

        // toggle piece on color bitboard
        _cBitboards[Piece.getColor(piece)] ^= bb;

        // pieces
        _pieces[square] = None.ID_White;

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
        _tBitboards[Piece.getType(piece)] ^= fromTo;
        _cBitboards[Piece.getColor(piece)] ^= fromTo;
        _pieces[from] = None.ID_White;
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
                char c = Piece.toChar(piece);
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

    @Override
    public Builder getBuilder() {
        return new Builder();
    }

    public long getKey() {
        return _stateStack.getFirst().getKey();
    }

    public static class Builder extends BoardBuilder<Board> {

        @NotRequired
        private String[] _tokens = null;

        @NotRequired
        private TokenFormat _tokenFormat = TokenFormat.None;

        @Override
        protected Board _buildT() {
            Board board = new Board();

            if (_tokens != null && _tokenFormat == TokenFormat.FEN) {
                // Set up pieces
                int squareIdx = 63;
                for (int i = 0; i < _tokens[0].length(); i++) {
                    // skip slashes
                    if (_tokens[0].charAt(i) == '/') {
                        continue;
                    }

                    // handle digit
                    if (Character.isDigit(_tokens[0].charAt(i))) {
                        squareIdx -= _tokens[0].charAt(i) - '0';
                        continue;
                    }

                    // get piece char as Piece Type
                    int piece = Piece.fromChar(_tokens[0].charAt(i));

                    // evaluate
                    board.addPiece(squareIdx ^ 7, piece);

                    squareIdx--;
                }

                // turn
                board.setTurn(_tokens[1].contains("w")
                        ? Color.White
                        : Color.Black);

                BoardState.Builder stateBuilder = new BoardState.Builder(board);
                stateBuilder
                        // castling
                        .castling(Castling.create(
                                _tokens[2].contains("K"),
                                _tokens[2].contains("Q"),
                                _tokens[2].contains("k"),
                                _tokens[2].contains("q")))

                        // en passant
                        .epSquare(!_tokens[3].contains("-")
                                ? Misc.Utils.toSquareIndex(_tokens[3])
                                : -1)

                        // plys for 50 move rule
                        .plys50(_tokens.length > 4 ? Integer.parseInt(_tokens[4]) : 0)

                        // fullmove counter
                        .ply(2 * (_tokens.length > 5 ? Integer.parseInt(_tokens[5]) - 1 : 0) + (board.getTurn() == Color.Black ? 1 : 0))

                        // zobrist key
                        .initKey(Zobrist.initialKey(board, stateBuilder))
                        
                        // three fold repitition
                        .threeFoldRepitition(false);

                board._stateStack.push(stateBuilder.build());
                board._ply = board._stateStack.getFirst().getPly();                
                board.positionsIncr(board.getKey());
            } 

            if (_tokens != null && _tokenFormat == TokenFormat.EPD) {
                // Set up pieces
                int squareIdx = 63;
                for (int i = 0; i < _tokens[0].length(); i++) {
                    // skip slashes
                    if (_tokens[0].charAt(i) == '/') {
                        continue;
                    }

                    // handle digit
                    if (Character.isDigit(_tokens[0].charAt(i))) {
                        squareIdx -= _tokens[0].charAt(i) - '0';
                        continue;
                    }

                    // get piece char as Piece Type
                    int piece = Piece.fromChar(_tokens[0].charAt(i));

                    // evaluate
                    board.addPiece(squareIdx ^ 7, piece);

                    squareIdx--;
                }

                // turn
                board.setTurn(_tokens[1].contains("w")
                        ? Color.White
                        : Color.Black);

                BoardState.Builder stateBuilder = new BoardState.Builder(board);
                stateBuilder
                        // castling
                        .castling(Castling.create(
                                _tokens[2].contains("K"),
                                _tokens[2].contains("Q"),
                                _tokens[2].contains("k"),
                                _tokens[2].contains("q")))
                        // en passant
                        .epSquare(!_tokens[3].contains("-")
                                ? Misc.Utils.toSquareIndex(_tokens[3])
                                : -1);

                EpdInfo epd = EpdInfo.ParseOperations(String.join(" ", Arrays.copyOfRange(_tokens, 4, _tokens.length)));
            }

            if (_tokenFormat == TokenFormat.None || _tokens == null) {
                BoardState.Builder stateBuilder = new BoardState.Builder(board)
                        .castling(Castling.empty())
                        .ply(0)
                        .plys50(0)
                        .threeFoldRepitition(false);
                stateBuilder.initKey(Zobrist.initialKey(board, stateBuilder));
                board._stateStack.push(stateBuilder.build());

                board.positionsIncr(board.getKey());
            }

            return board;
        }

        @Override
        public Builder tokens(String[] tokens, TokenFormat format) {
            _tokens = tokens;
            _tokenFormat = format;
            return this;
        }

        public Builder fen(String fen) {
            return tokens(fen.split(" "), TokenFormat.FEN);
        }
    }


    public String fen() {
        StringBuilder sb = new StringBuilder();
        for (int rank = 7; rank >= 0; rank--) {
            int emptyCount = 0;
            for (int file = 0; file <= 7; file++) {
                int square = Misc.Utils.sqaureIndex0(rank, file);
                int piece = getPieceID(square);
                if (piece == None.ID_Type) {
                    emptyCount++;
                } else {
                    if (emptyCount > 0) {
                        sb.append(emptyCount);
                        emptyCount = 0;
                    }
                    sb.append((char)Piece.toChar(piece));
                }
            }
            if (emptyCount > 0) {
                sb.append(emptyCount);
            }
            if (rank > 0) {
                sb.append("/");
            }
        }
        sb.append(" ");
        sb.append(getTurn() == Color.White ? "w" : "b");
        sb.append(" ");
        BitSet castling = getCastling();
        String castlingStr = "";
        if (Castling.get(castling, Castling.KingSide, Color.White)) {
            castlingStr += "K"; 
        }
        if (Castling.get(castling, Castling.QueenSide, Color.White)) {
            castlingStr += "Q";
        }
        if (Castling.get(castling, Castling.KingSide, Color.Black)) {
            castlingStr += "k";
        }
        if (Castling.get(castling, Castling.QueenSide, Color.Black)) {
            castlingStr += "q";
        }
        if (castlingStr.equals("")) {
            castlingStr = "-";
        }
        sb.append(castlingStr);
        sb.append(" ");
        int epSquare = getEnPassantSquare();
        long epAttackers = getBitboard(Pawn.ID_Type, getTurn()); 
        if (epSquare != -1) {
            epAttackers &= Masks.Ranks[4 - getTurn()];
            epAttackers &= Masks.Passants[Utils.file(epSquare)];
        }
        sb.append(epSquare == -1 || epAttackers == 0 ? "-" : Misc.Utils.fromSquareIndex(epSquare));
        sb.append(" ");
        sb.append(_stateStack.getFirst().getPlys50());
        sb.append(" ");
        sb.append(_stateStack.getFirst().getPly() / 2 + 1);
        return sb.toString();
    }

}
