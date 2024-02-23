package UCI;

public abstract class UciCommand extends ICommand
{
    protected Engine.IBoard _board;
    
    public UciCommand(Engine.IBoard board) {
        _board = board;
    }
}
