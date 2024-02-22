
public class UciQuitCommand extends UciCommand
{
    public UciQuitCommand(IBoard board)
    {
        super(board);
    }
    
    public void run() {
        System.exit(0);
    }
}
