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
        for (String move : _moves) {
            _board.get().makeMove(_board.get().getMoveDecoder().decode(move));
        }
    }

    protected abstract void setPosition();
}
