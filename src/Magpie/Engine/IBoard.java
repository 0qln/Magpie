package Engine;

public interface IBoard
{
    public IMoveDecoder getMoveDecoder();

    public void makeMove(short move);
    public void undoMove(short move);

    public void addPiece(int square, int piece);
    public int getPiece(int square);
    public void removePiece(int square);
    public void setTurn(int color);
    public int getTurn();
    public void setCastlingRights(int pieceType, int color, boolean active);
    public void setEnpassant(int square);
    public int getEnPassantSquare();
    public void setPlys50(int plys);
}
