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
        _board.set(_board.get()
            .getBuilder()
            .fen(_fen)
            .build());
    }
}
