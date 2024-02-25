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
    // The square where an en passant capture is possible, if non are possible -1
    private int _epSquare = -1;
    // The game ply
    private int _ply = 0;
    // The side the move
    private int _turn = Color.White;
    // A linked list to store the previous states of the game
    private LinkedList<BoardState> _stateStack = new LinkedList<BoardState>();
    
    
    public Board()
    {
    }
    

    @Override
    public void makeMove(short move) {
        BoardState bs = new BoardState();
        _stateStack.push(bs);
    }
    
    @Override
    public void undoMove(short move) {
        _stateStack.pop();
    }


    @Override
    public IMoveDecoder getMoveDecoder() {
        return str -> {
            int from = Misc.Utils.toSquareIndex(str.substring(0, 2));
            int to = Misc.Utils.toSquareIndex(str.substring(2, 4));
            // TODO: 
            // Use board context to determine the flag [e.g. castling, capturing, promotion]
            int flag = -1;
            return Move.create(from, to, flag);
        };
    }


    @Override
    public void setCastlingRights(int pieceType, int color, boolean active) {
        // TODO Auto-generated method stub
        
    }


    @Override
    public void setEnpassant(int square) {
        // TODO Auto-generated method stub
        
    }


    @Override
    public void addPiece(int square, int piece) {
        assert(piece != Piece.WNone && piece != Piece.BNone);

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
        _pieces[square] = Piece.WNone;

        // piece count
        _pieceCount[piece]--;
    }


    @Override
    public int getPiece(int square) {
        return _pieces[square];
    }


    @Override
    public void setPlys50(int plys) {
        // TODO Auto-generated method stub
        
    }


    @Override
    public void setTurn(int color) {
        // TODO Auto-generated method stub
        
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
}
