package Engine;

import static Engine.Utils.countBits;
import static Engine.Utils.file;
import static Engine.Utils.lsb;
import static Engine.Utils.popLsb;
import static Engine.Utils.target;

import java.util.*;
import java.util.function.BiConsumer;

public class Board implements IBoard {
    
    private Board board = this;

    // Keep track of positions for three fold repitition.
    private ThreeFoldPositionTable _tfpTable = ThreeFoldPositionTable.ofOrder(20);

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
        final int nus, us = _turn;
        final int from = Move.getFrom(move);
        final int to = Move.getTo(move);
        final int flag = Move.getFlag(move);
        final int rNormF = from / 8 * 8;
        final int movingPiece = getPieceID(from);
        final int targetPiece = getPieceID(to);
        final BoardState oldState = _stateStack.getFirst();
        final int oldCastling = oldState.getCastling();

        // These will be updated as we go along.
        int newCastling = oldCastling;
        long key = oldState.getKey();

        // The xor operation is it's own inverse, thus we just apply the operation on
        // every new move, regardless of the current stm, becuase it will inverse, the
        // black color if the current stm is white.
        // Change turn.
        nus = _turn ^= 1;
        key ^= Zobrist.stm;

        // Create new board state
        final BoardState.Builder newStateBuilder = new BoardState.Builder(this)
                .ply(++_ply)
                .plys50(oldState.getPlys50() + 1);
 
        // Handle captures
        if (Move.isCapture(flag)) {
            
            final int capturedPiece, capturedSquare;

            capturedPiece = flag == Move.EN_PASSANT_FLAG 
                ? Piece.create(Pawn.ID_Type, nus) 
                : targetPiece;
            
            capturedSquare = flag == Move.EN_PASSANT_FLAG
                ? to + (us * 2 - 1) * 8
                : to;

            newStateBuilder
                    // Remember captured piece
                    .captured(capturedPiece)
                    // If a piece get's captured, the 50 move rule counter needs to be reset
                    .plys50(0);

            // Update castling if a rook has been captured
            newCastling = Castling.update(newCastling, capturedSquare, nus);

            // Remove destination piece for captures
            removePiece(capturedSquare);

            key ^= Zobrist.pieceSQ[capturedPiece][capturedSquare];
        }

        // Move The piece
        movePiece(from, to);

        key ^= Zobrist.pieceSQ[movingPiece][from];
        key ^= Zobrist.pieceSQ[movingPiece][to];

        if (oldState.getEpSquare() != -1)
            key ^= Zobrist.enPassant[file(oldState.getEpSquare())];
                     
        switch (Piece.getType(movingPiece)) {
            // Handle castling rights
            case King.ID_Type:
                newCastling = Castling.setFalse(newCastling, Castling.KingSide, us);
                newCastling = Castling.setFalse(newCastling, Castling.QueenSide, us);
                // Move the rook
                final int rFrom, rTo, rook;
                switch (flag) {
                    case Move.KING_CASTLE_FLAG:
                        rFrom = rNormF + Files.H; 
                        rTo = rNormF + Files.F;
                        rook = _pieces[rFrom];
                        // (On the board)
                        movePiece(rFrom, rTo);
                        // (In the hash)
                        key ^= Zobrist.pieceSQ[rook][rFrom];
                        key ^= Zobrist.pieceSQ[rook][rTo];
                        break;
                    case Move.QUEEN_CASTLE_FLAG:
                        rFrom = rNormF + Files.A; 
                        rTo = rNormF + Files.D;
                        rook = _pieces[rFrom];
                        // (On the board)
                        movePiece(rFrom, rTo);
                        // (In the hash)
                        key ^= Zobrist.pieceSQ[rook][rFrom];
                        key ^= Zobrist.pieceSQ[rook][rTo];
                        break;
                }
                break;
            case Rook.ID_Type:
                switch (us) {
                    case Color.White:
                        if (from == Squares.A1) newCastling = Castling.setFalse(newCastling, Castling.QueenSide, Color.White);
                        if (from == Squares.H1) newCastling = Castling.setFalse(newCastling, Castling.KingSide, Color.White);
                        break;
                    case Color.Black:
                        if (from == Squares.A8) newCastling = Castling.setFalse(newCastling, Castling.QueenSide, Color.Black);
                        if (from == Squares.H8) newCastling = Castling.setFalse(newCastling, Castling.KingSide, Color.Black);
                        break;
                }
                break;
            
            // Pawns extras
            case Pawn.ID_Type:
                // Handle double pawn pushes
                if (flag == Move.DOUBLE_PAWN_PUSH_FLAG) {
                    newStateBuilder.epSquare(to + (us * 2 - 1) * 8);
                    key ^= Zobrist.enPassant[file(to)];
                }

                // Handle promotions
                if (flag >= Move.PROMOTION_KNIGHT_FLAG && flag <= Move.CAPTURE_PROMOTION_QUEEN_FLAG) {
                    int promotion = Piece.create(Move.getPromotion(move), us);
                    removePiece(to);
                    addPiece(to, promotion);
                    // Replace pawn signature with promotion siganture
                    key ^= Zobrist.pieceSQ[movingPiece][to];
                    key ^= Zobrist.pieceSQ[promotion][to];
                }

                // If a pawn get's moved, the 50 move rule counter needs to be reset
                newStateBuilder.plys50(0);
                break;
        }

        // In hash, update castling, if it has changed
        if (newCastling != oldCastling) {
            key ^= Zobrist.castling[Castling.key(oldCastling)];
            key ^= Zobrist.castling[Castling.key(newCastling)];
        }

        // Remember new position
        _tfpTable.increment(key);

        newStateBuilder
               // Three fold repitition
                .threeFoldRepitition(_tfpTable.get(key) >= 3)
                // Initiate key for new board state
                .initKey(key)
                // Set castling
                .castling(newCastling);

        // Update board state
        _stateStack.push(newStateBuilder.build());
    }

    @Override
    public void undoMove(short move) {
        // Forget latest position
        _tfpTable.decrement(getKey());

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
        return countBits(_stateStack.getFirst().getCastling());
    }

    public int getCastling() {
        return _stateStack.getFirst().getCastling();
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
        result += "   a b c d e f g h";
        return result;
    }

    public boolean canCastle(int side, int color) {
        return Castling.hasSpace(side, color, getOccupancy())
                && Castling.getB(getCastling(), side, color);
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
        
        public EpdInfo epdInfoResult = null;

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
                board._tfpTable.increment(board.getKey());
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
                
                stateBuilder
                        // full move number
                        .ply(2 * (epd.fmvn - 1) + (board.getTurn() == Color.Black ? 1 : 0))

                        // plys for 50 move rule
                        .plys50(epd.hmvc)

                        // zobrist key
                        .initKey(Zobrist.initialKey(board, stateBuilder))
                        
                        // three fold repitition
                        .threeFoldRepitition(false);

                board._stateStack.push(stateBuilder.build());
                board._ply = board._stateStack.getFirst().getPly();                
                board._tfpTable.increment(board.getKey());
                
                // Play the supplied move.
                if (epd.sm != null) {
                    IMoveDecoder decoder = board.getMoveDecoder(MoveFormat.StandardAlgebraicNotation);
                    board.makeMove(decoder.decode(epd.sm));                
                }
                
                epdInfoResult = epd;
            }

            if (_tokenFormat == TokenFormat.None || _tokens == null) {
                BoardState.Builder stateBuilder = new BoardState.Builder(board)
                        .castling(Castling.empty())
                        .ply(0)
                        .plys50(0)
                        .threeFoldRepitition(false);
                stateBuilder.initKey(Zobrist.initialKey(board, stateBuilder));
                board._stateStack.push(stateBuilder.build());
                board._tfpTable.increment(board.getKey());
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
        
        public Builder epd(String epd) {
            return tokens(epd.split(" "), TokenFormat.EPD);
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
        int castling = getCastling();
        String castlingStr = "";
        if (Castling.getB(castling, Castling.KingSide, Color.White)) {
            castlingStr += "K"; 
        }
        if (Castling.getB(castling, Castling.QueenSide, Color.White)) {
            castlingStr += "Q";
        }
        if (Castling.getB(castling, Castling.KingSide, Color.Black)) {
            castlingStr += "k";
        }
        if (Castling.getB(castling, Castling.QueenSide, Color.Black)) {
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
