package UCI;

public class UciStartposCommand extends UciPositionCommand
{
    public UciStartposCommand(Engine.IBoard board, String[] moves) {
        super(board, moves);
    }

    @Override
    protected void runExtended() {
        Engine.FenDecoder
            .decode(new String[] { "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR", "w", "KQkq", "-", "0", "1" })
            .setUpFen(_board);
    }
}