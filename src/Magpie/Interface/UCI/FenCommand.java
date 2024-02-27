package Interface.UCI;

import Misc.Ptr;

public class FenCommand extends PositionCommand
{
    private String[] _fen;

    public FenCommand(Ptr<Engine.IBoard> board, String[] moves, String[] fen) {
        super(board, moves);
        _fen = fen;
    }
    
    @Override
    protected void setPosition() {
        Engine.IBoard newPosition = _board.get().getBuilder().build();

        Engine.FenDecoder
            .decode(_fen)
            .setUpFen(newPosition);

        _board.set(newPosition);
    }
}
