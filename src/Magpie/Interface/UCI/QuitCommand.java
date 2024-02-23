package Interface.UCI;

public class QuitCommand extends Command
{
    public QuitCommand(Engine.IBoard board)
    {
        super(board);
    }
    
    public void run() {
        System.exit(0);
    }
}
