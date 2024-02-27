package Interface.UCI;

import Interface.ICommand;
import Misc.Ptr;

public abstract class Command extends ICommand
{
    protected Ptr<Engine.IBoard> _board;
    
    public Command(Ptr<Engine.IBoard> board) {
        _board = board;
    }
}
