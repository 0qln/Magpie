package Interface.Custom;

import Engine.IBoard;
import Misc.Ptr;

public abstract class InfoCommand extends Command {

    public InfoCommand(Ptr<IBoard> board) {
        super(board);
    }

}
