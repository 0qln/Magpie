package Interface.Custom;

import Engine.IBoard;
import Misc.Ptr;
import static Engine.Utils.*;

public class CheckInfoCommand extends InfoCommand {

    public CheckInfoCommand(Ptr<IBoard> board) {
        super(board);
    }

    @Override
    public void run() {

        Engine.Board board = _board.getAs();
        long[] checkers = { board.getCheckers() };

        System.out.println("Checker Info");

        while (checkers[0] != 0) {
            int checker = popLsb(checkers);
            new SquareInfoResponse(checker, "Checker").send();
            printBB(target(checker));
        }

    }

}
