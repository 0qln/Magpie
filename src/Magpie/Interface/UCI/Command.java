package Interface.UCI;

import Interface.ICommand;

public abstract class Command extends ICommand
{
    protected Engine.IBoard _board;
    
    public Command(Engine.IBoard board) {
        _board = board;
    }
}
