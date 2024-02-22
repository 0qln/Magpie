import java.util.*;

public interface IMoveGenerator<TMove>
{
    ArrayList<TMove> getPseudoLegalMoves(IBoard<TMove> board, int square);
}
