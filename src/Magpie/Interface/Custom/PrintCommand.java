package Interface.Custom;

import Engine.IBoard;

public class PrintCommand extends Command
{
    public PrintCommand(IBoard board) {
        super(board);
    }

    public void run() {
        System.out.println(_board.toString());
    }
}
