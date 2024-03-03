package Interface.UCI;

import Misc.Ptr;

public abstract class PositionCommand extends Command
{
    // TODO: 
    // Only create new position if a new move was just appended
    // (which will be the case often on for real applications)
    private static PositionCommand lastPosition;

    private String[] _moves;
    
    public PositionCommand(Ptr<Engine.IBoard> board, String[] moves) {
        super(board);
        _moves = moves;
    }
    
    public void run() {
        setPosition();
        for (String moveStr : _moves) {
            var move = _board.get().getMoveDecoder().decode(moveStr);
            _board.get().makeMove(move);
        }
    }

    protected abstract void setPosition();
}
