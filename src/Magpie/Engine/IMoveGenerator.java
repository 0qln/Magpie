package Engine;

public interface IMoveGenerator<TBoard>
{
    void generate(TBoard board, int square);
}
