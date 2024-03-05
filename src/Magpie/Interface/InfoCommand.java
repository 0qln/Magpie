package Interface;

import Engine.IBoard;
import Misc.Ptr;

public class InfoCommand extends Command {

    static {
        Signature.register("info", InfoCommand.class, new Builder<>(() -> new InfoCommand()));
    }

    @Override
    public boolean parseArgs(String[] args) {
        if (args.length < 1) {
            return false;
        }

        switch (args[0]) {
            case "check":
                break;
        

            default:
                break;
        }

        return false;
    }

    @Override
    public void run() {
                
    }

    // @Override
    // public void run() {
    //     Engine.Board board = _board.getAs();
    //     System.out.println(Engine.Castling.toString(board.getCastling()));
    // }

}
