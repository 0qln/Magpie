package Interface.UCI;

import Interface.Custom.PrintCommand;

public class StartposCommand extends PositionCommand
{
    public StartposCommand(Engine.IBoard board, String[] moves) {
        super(board, moves);
    }

    @Override
    protected void runExtended() {
        Engine.FenDecoder
            .decode(new String[] { "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR", "w", "KQkq", "-", "0", "1" })
            .setUpFen(_board);
    }
}
