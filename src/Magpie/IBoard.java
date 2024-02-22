public interface IBoard<TMove>
{
    public IMoveDecoder<TMove> getMoveDecoder();

    public void makeMove(TMove move);
    public void undoMove(TMove move);

    public void setPiece(int square, int piece);
    public void setTurn(int color);
    public void setCastlingRights(int pieceType, int color, boolean active);
    public void setEnpassant(int square);
    public void setPlys50(int plys);
}
