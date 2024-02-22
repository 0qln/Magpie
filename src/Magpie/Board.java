import java.util.*;

public class Board implements IBoard<Short>
{
    private long[] _cBitboards = new long[2];
    private long[] _pBitboards = new long[7];
 
    private LinkedList<BoardState> _stateStack = new LinkedList<BoardState>();
    
    
    public Board()
    {
    }
    

    @Override
    public void makeMove(Short move) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'makeMove'");
    }
    
    @Override
    public void undoMove(Short move) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'undoMove'");
    }


    @Override
    public IMoveDecoder<Short> getMoveDecoder() {
        // TODO Auto-generated method stub
        return null;
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
    public void setPiece(int square, int piece) {
        // TODO Auto-generated method stub
        
    }


    @Override
    public void setPlys50(int plys) {
        // TODO Auto-generated method stub
        
    }


    @Override
    public void setTurn(int color) {
        // TODO Auto-generated method stub
        
    }
}
