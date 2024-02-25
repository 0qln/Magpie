package Engine;

public interface IMoveGenerator
{
    short[] getPseudoLegalMoves(IBoard board, int square);
}
