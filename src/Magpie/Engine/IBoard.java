package Engine;

public interface IBoard
{
    public IMoveDecoder getMoveDecoder(MoveFormat format);
    public IMoveEncoder getMoveEncoder();

    public void makeMove(short move);
    public void undoMove(short move);

    public void addPiece(int square, int piece);
    public int getPieceID(int square);
    public void removePiece(int square);
    public int getTurn();
    public int getEnPassantSquare();
    public void setEnpassant(int squareIndex);
    public void setPlys50(int int1);
    public void setTurn(int black);

    public BoardBuilder<?> getBuilder();
}
