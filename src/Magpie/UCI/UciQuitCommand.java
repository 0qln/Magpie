package UCI;

public class UciQuitCommand extends UciCommand
{
    public UciQuitCommand(Engine.IBoard board)
    {
        super(board);
    }
    
    public void run() {
        System.exit(0);
    }
}
