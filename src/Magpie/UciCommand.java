public abstract class UciCommand extends ICommand
{
    protected IBoard _board;
    
    public UciCommand(IBoard board) {
        _board = board;
    }
}
