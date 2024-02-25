package Interface.UCI;

import Interface.Main;

public class QuitCommand extends Command
{
    public QuitCommand(Engine.IBoard board)
    {
        super(board);
    }
    
    public void run() {
        Main.scanner.close();
        System.exit(0);
    }
}
