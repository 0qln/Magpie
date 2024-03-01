package Interface.UCI;

import Misc.Ptr;

public class StartposCommand extends PositionCommand
{
    private static final String[] FEN = 
        new String[] { "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR", "w", "KQkq", "-", "0", "1" };

    public StartposCommand(Ptr<Engine.IBoard> board, String[] moves) {
        super(board, moves);
    }

    @Override
    protected void setPosition() {
        _board.set(_board.get()
            .getBuilder()
            .fen(FEN)
            .build());
    }
}
