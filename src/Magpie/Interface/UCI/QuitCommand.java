package Interface.UCI;

import Interface.Main;
import Misc.Ptr;

public class QuitCommand extends Command
{
    public QuitCommand(Ptr<Engine.IBoard> board)
    {
        super(board);
    }
    
    public void run() {
        Main.scanner.close();
        System.exit(0);
    }
}
