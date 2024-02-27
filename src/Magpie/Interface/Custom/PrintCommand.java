package Interface.Custom;

import Misc.Ptr;

public class PrintCommand extends Command
{
    public PrintCommand(Ptr<Engine.IBoard> board) {
        super(board);
    }

    public void run() {
        System.out.println(_board.get().toString());
    }
}
