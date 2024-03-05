package Interface;

import Interface.Command.*;
import Misc.Ptr;

/*
 * piece [get <square> | add <square> <piece> | remove <square> ]
 */
public class PieceCommand extends Command {

    static {
        Signature.register("piece", PieceCommand.class, new Builder<>(() -> new PieceCommand()));
    }

    @Override
    public boolean parseArgs(String[] args) {
        return false;
    }

    @Override
    public void run() {

    }

}
