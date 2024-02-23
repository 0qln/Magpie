package Interface.UCI;

public class FenCommand extends PositionCommand
{
    private String[] _fen;

    public FenCommand(Engine.IBoard board, String[] moves, String[] fen) {
        super(board, moves);
        _fen = fen;
    }
    
    @Override
    protected void runExtended() {
        Engine.FenDecoder
            .decode(_fen)
            .setUpFen(_board);
    }
}
