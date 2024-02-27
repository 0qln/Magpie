package Interface.UCI;

import Misc.Ptr;

public class IsreadyCommand extends Command
{
    public IsreadyCommand(Ptr<Engine.IBoard> board) {
        super(board);
    }

    public void run() {
        new ReadyokResponse().send();
    }
}
