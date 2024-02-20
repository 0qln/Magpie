public abstract class UciCommand extends ICommand
{
    private IBoard _board;
    
    public UciCommand(IBoard board) {
        _board = board;
    }
}
