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
        Engine.IBoard newPosition = _board.get().getBuilder().build();

        Engine.FenDecoder
            .decode(FEN)
            .setUpFen(newPosition);

        _board.set(newPosition);
    }
}
