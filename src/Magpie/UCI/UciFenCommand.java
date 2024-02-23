package UCI;

public class UciFenCommand extends UciPositionCommand
{
    private String[] _fen;

    public UciFenCommand(Engine.IBoard board, String[] moves, String[] fen) {
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
