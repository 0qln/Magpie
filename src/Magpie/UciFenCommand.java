
public class UciFenCommand extends UciPositionCommand
{
    private String[] _fen;

    public UciFenCommand(IBoard board, String[] moves, String[] fen) {
        super(board, moves);
        _fen = fen;
    }
    
    @Override
    protected void runExtended() {
        FenDecoder
            .decode(_fen)
            .setUpFen(_board);
    }
}
