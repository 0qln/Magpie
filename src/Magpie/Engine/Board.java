package Engine;

import java.util.*;

public class Board implements IBoard
{
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

    
    public Board() throws IllegalArgumentException, IllegalAccessException
    {
        _stateStack.push(new BoardState.Builder(this)
            .castling(new byte[] {1,1,1,1})
            .ply(0)
            .plys50(0)
            .build()
        );
    }
    

    public void makeMove(short move) {
        // Increment game counters
        _turn = Color.NOT(_turn);
        _ply++;

        // Create new board state
        BoardState.Builder newState = new BoardState.Builder(this);
        newState
            .givesCheck(false) // TODO
            .castling(_stateStack.getLast().getCastling().toByteArray())
            .ply(_ply)
            .plys50(_ply);
       
        // Get context
        final int us = _turn, nus = Color.NOT(us);
        final int from = Move.getFrom(move); 
        final int to = Move.getTo(move);
        final int flag = Move.getFlag(move);
        final int fromRank = from / 8;
        int capturedPiece = flag == Move.EN_PASSANT_FLAG ? Piece.create(nus, PieceType.Pawn) : getPiece(to);
        final int movingPiece = getPiece(from);
        
        // Handle castling
        if (flag == Move.KING_CASTLE_FLAG) {
            removePiece (fromRank * 8 + Files.E);
            addPiece    (fromRank * 8 + Files.G, Piece.create(PieceType.King, us));
            removePiece (fromRank * 8 + Files.H);
            addPiece    (fromRank * 8 + Files.F, Piece.create(PieceType.Rook, us));
            newState.getCastling().set((us << 1) | 1, false);
            capturedPiece = Piece.None;
        }
        else if (flag == Move.QUEEN_CASTLE_FLAG) {
            removePiece (fromRank * 8 + Files.E);
            addPiece    (fromRank * 8 + Files.C, Piece.create(PieceType.King, us));
            removePiece (fromRank * 8 + Files.A);
            addPiece    (fromRank * 8 + Files.D, Piece.create(PieceType.Rook, us));
            newState.getCastling().set((us << 1) | 0, false);
            capturedPiece = Piece.None;
        }

        // Handle captures
        // Also get's activated on a promotion with capture, which is right.
        else if (Piece.getType(capturedPiece) != PieceType.None) {
            int captureSquare = to;
            
            // Handle en passant captures
            if (flag == Move.EN_PASSANT_FLAG) {
                captureSquare += (us * 2 - 1) * 8;
            }
            
            // Remove destination piece for captures
            removePiece(captureSquare, capturedPiece);

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
        if (Piece.getType(movingPiece) == PieceType.Pawn) {
            // Handle double pawn pushes
            if (flag == Move.DOUBLE_PAWN_PUSH_FLAG)
                newState.epSquare(to + (us * 2 - 1) * 8);

            // Handle promotions
            if (flag >= Move.PROMOTION_KNIGHT_FLAG && flag <= Move.CAPTURE_PROMOTION_QUEEN_FLAG) {
                int flagHack = flag <= Move.PROMOTION_QUEEN_FLAG ? flag : flag - 4;
                int newPiece = Piece.create(us, flagHack);
                removePiece(to);
                addPiece(to, newPiece);
            }

            // If a pawn get's moved, the 50 move rule counter needs to be reset
            newState.plys50(0);
        }
        
        // Update board state
        _stateStack.push(newState.buildUnchecked());
    }

    
    @Override
    public void undoMove(short move) {
        // Increment game counters
        _turn = Color.NOT(_turn);
        _ply--;

        // Get context
        int us = _turn;
        final int from = Move.getFrom(move); 
        final int fromRank = from / 8;
        final int to = Move.getTo(move);
        final int flag = Move.getFlag(move);

        // Handle promotions
        if (flag >= Move.PROMOTION_KNIGHT_FLAG && flag <= Move.CAPTURE_PROMOTION_QUEEN_FLAG) {
            // Replace the promotion with a pawn, the pawn will be moved later.
            removePiece(to);
            addPiece(to, Piece.create(us, PieceType.Pawn));
        }

        // Handle castling
        if (flag == Move.KING_CASTLE_FLAG) {
            removePiece (fromRank * 8 + Files.G);
            addPiece    (fromRank * 8 + Files.E, Piece.create(PieceType.King, us));
            removePiece (fromRank * 8 + Files.F);
            addPiece    (fromRank * 8 + Files.H, Piece.create(PieceType.Rook, us));
        }
        else if (flag == Move.QUEEN_CASTLE_FLAG) {
            removePiece (fromRank * 8 + Files.C);
            addPiece    (fromRank * 8 + Files.E, Piece.create(PieceType.King, us));
            removePiece (fromRank * 8 + Files.D);
            addPiece    (fromRank * 8 + Files.A, Piece.create(PieceType.Rook, us));
        }

        // Move the piece
        else {
            movePiece(to, from);

            // Handle captures
            if (_stateStack.getLast().getCaptured() != Piece.None) {
                int captureSquare = to;

                // Handle en passant captures
                if (flag == Move.EN_PASSANT_FLAG) {
                    captureSquare += (us * 2 - 1) * 8;
                }

                addPiece(captureSquare, _stateStack.getLast().getCaptured());
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
    public IMoveDecoder getMoveDecoder() {
        return str -> {
            int from = Misc.Utils.toSquareIndex(str.substring(0, 2));
            int to = Misc.Utils.toSquareIndex(str.substring(2, 4));
            
            // Use board context to determine the flag [e.g. castling, capturing, promotion]
            int movingPieceType = Piece.getType(getPiece(from));
            int destPieceType = Piece.getType(getPiece(to));
            int absDistance = Math.abs(from - to);
            boolean captures = destPieceType == PieceType.None;
            int flag = captures ? Move.QUIET_MOVE_FLAG : Move.CAPTURE_FLAG;

            // Pawn extras
            if (movingPieceType == PieceType.Pawn) {
                // Double pawn push
                if (absDistance == 16) {
                    flag = Move.DOUBLE_PAWN_PUSH_FLAG;
                }
                // En passant capture
                else if ((absDistance == 7 || absDistance == 9) && destPieceType == PieceType.None) {
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
            else if (movingPieceType == PieceType.King) {
                if (absDistance == 2) {
                    flag = Move.KING_CASTLE_FLAG;
                }
                else if (absDistance == 3) {
                    flag = Move.QUEEN_CASTLE_FLAG;
                }
            }

            return Move.create(from, to, flag);
        };
    }


    public int getCastlingCardinality() {
        return _stateStack.getLast().getCastling().cardinality();
    }


    public BitSet getCastling() {
        return (BitSet)_stateStack.getLast().getCastling().clone();
    }


    @Override
    public int getEnPassantSquare() {
        return _stateStack.getLast().getEpSquare();
    }


    @Override
    public void addPiece(int square, int piece) {
        assert(piece != Piece.None && piece != Piece.BNone);

        int type = Piece.getType(piece), color = Piece.getColor(piece);
        int prevPiece = getPiece(square);

        // tBitboards
        Utils.deactivateBit(_tBitboards, Piece.getType(prevPiece), square);
        Utils.activateBit(_tBitboards, type, square);

        // cBitboards
        Utils.deactivateBit(_cBitboards, Piece.getColor(prevPiece), square);
        Utils.activateBit(_cBitboards, color, square);

        // pieces
        _pieces[square] = piece;

        // piece count
        _pieceCount[piece]++;
    }

    @Override
    public void removePiece(int square) {
        int Piece = getPiece(square);
        removePiece(square, Piece);
    }

    public void removePiece(int square, int piece) {
        int type = Piece.getType(piece), color = Piece.getColor(piece);

        // tBitboards
        Utils.deactivateBit(_tBitboards, type, square);

        // cBitboards
        Utils.deactivateBit(_cBitboards, color, square);

        // pieces
        _pieces[square] = Piece.None;

        // piece count
        _pieceCount[piece]--;
    }


    @Override
    public int getPiece(int square) {
        return _pieces[square];
    }


    private void movePiece(int from, int to) {
        int piece = _pieces[from];
        long fromTo = ((long)1 << from) | ((long)1 << to);
        _tBitboards[Piece.getType(piece)] ^= fromTo;
        _cBitboards[Piece.getColor(piece)] ^= fromTo; 
        _pieces[from] = Piece.None;
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
                int piece = getPiece(square);
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
        _stateStack.getLast().setCastlingRights(king, white, b);
    }


    @Override
    public void setEnpassant(int squareIndex) {
        _stateStack.getLast().setEpSquare(squareIndex);
    }


    @Override
    public void setPlys50(int value) {
        _stateStack.getLast().setPlys50(value);
    }


    @Override
    public void setTurn(int color) {
        _turn = color;
    }


    @Override
    public IBoardBuilder getBuilder() {
        return new Builder();
    }


    public static class Builder implements IBoardBuilder {
        @Override
        public IBoard build() {
            try {
                return new Board();
            } catch (IllegalArgumentException | IllegalAccessException e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}
