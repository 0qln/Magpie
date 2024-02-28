package Engine;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import Misc.Ptr;

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
    

    public long perft(int depth, BiConsumer<Short, Long> callback) {
        if (depth == 0) {
            return 0;
        } 

        long moveC = 0, c;
        for (short move : MoveList.generate(this).getMoves()) {
            System.out.println("NEW");
            new Interface.Custom.PrintCommand(Ptr.to(this)).run();
            makeMove(move);
            new Interface.Custom.PrintCommand(Ptr.to(this)).run();
            moveC += (c = perft(depth-1));
            callback.accept(move, c);
            undoMove(move);
            new Interface.Custom.PrintCommand(Ptr.to(this)).run();
        }

        return moveC;
    }


    private long perft(int depth) {
        if (depth == 0) {
            return 0;
        } 

        long moveC = 0;
        for (short move : MoveList.generate(this).getMoves()) {
            makeMove(move);
            moveC += perft(depth-1);
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
        final int movingPiece = getPiece(from);
        int capturedPiece = flag == Move.EN_PASSANT_FLAG ? Piece.create(PieceType.Pawn, nus) : getPiece(to);
        
        // Increment game counters
        _turn = Color.NOT(_turn);
        _ply++;

        // Create new board state
        BoardState.Builder newState = new BoardState.Builder(this);
        newState
            .givesCheck(false) // TODO
            .castling(_stateStack.getFirst().getCastling().toByteArray())
            .ply(_ply)
            .plys50(_ply);
       
        // Handle castling
        if (flag == Move.KING_CASTLE_FLAG) {
            movePiece(rNormF + Files.E, rNormF + Files.G);
            movePiece(rNormF + Files.H, rNormF + Files.F);
            newState.getCastling().set((us << 1) | 1, false);
            capturedPiece = Piece.None[0];
        }
        else if (flag == Move.QUEEN_CASTLE_FLAG) {
            movePiece(rNormF + Files.E, rNormF + Files.C);
            movePiece(rNormF + Files.A, rNormF + Files.D);
            newState.getCastling().set((us << 1) | 0, false);
            capturedPiece = Piece.None[0];
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
        if (Piece.getType(movingPiece) == PieceType.Pawn) {
            // Handle double pawn pushes
            if (flag == Move.DOUBLE_PAWN_PUSH_FLAG)
                newState.epSquare(to + (us * 2 - 1) * 8);

            // Handle promotions
            if (flag >= Move.PROMOTION_KNIGHT_FLAG && flag <= Move.CAPTURE_PROMOTION_QUEEN_FLAG) {
                int flagHack = flag <= Move.PROMOTION_QUEEN_FLAG ? flag : flag - 4;
                int newPiece = Piece.create(flagHack, us);
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
            addPiece(to, Piece.create(PieceType.Pawn, us));
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
            final int captured = _stateStack.getFirst().getCaptured();
            if (Piece.getType(captured) != PieceType.None) {
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
    public IMoveDecoder getMoveDecoder() {
        return str -> {
            int from = Misc.Utils.toSquareIndex(str.substring(0, 2));
            int to = Misc.Utils.toSquareIndex(str.substring(2, 4));
            
            // Use board context to determine the flag [e.g. castling, capturing, promotion]
            int movingPieceType = Piece.getType(getPiece(from));
            int destPieceType = Piece.getType(getPiece(to));
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
                }
                else if (to % 8 == Files.C) {
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
        return (BitSet)_stateStack.getFirst().getCastling().clone();
    }


    @Override
    public int getEnPassantSquare() {
        return _stateStack.getFirst().getEpSquare();
    }


    @Override
    public void addPiece(int square, int piece) {
        long bb = 1L << square;

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
        int piece = getPiece(square);
        long bb = 1L << piece;

        // toggle piece on type bitboard
        _tBitboards[Piece.getType(piece)] ^= bb;

        // toggle piece on color bitboard
        _cBitboards[Piece.getColor(piece)] ^= bb;

        // pieces
        _pieces[square] = Piece.None[0];

        // decrement piece count
        _pieceCount[piece]--;
    }


    @Override
    public int getPiece(int square) {
        return _pieces[square];
    }


    private void movePiece(int from, int to) {
        int piece = _pieces[from];
        long fromTo = (1L << from) | (1L << to);
        _tBitboards[Piece.getType(piece)] ^= fromTo;
        _cBitboards[Piece.getColor(piece)] ^= fromTo; 
        _pieces[from] = Piece.None[0];
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
        _stateStack.getFirst().setCastlingRights(king, white, b);
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
