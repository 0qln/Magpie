package Interface;

import Engine.IBoard;
import Misc.Ptr;
import static Engine.Utils.*;

public class CheckInfoCommand  {

    // public CheckInfoCommand(Ptr<IBoard> board) {
    //     super(board);
    // }

    // @Override
    // public void run() {

    //     Engine.Board board = _board.getAs();
    //     long[] checkers = { board.getCheckers() };

    //     while (checkers[0] != 0) {
    //         int checker = popLsb(checkers);
    //         new SquareInfoResponse(checker, "Checker").send();
    //         printBB(target(checker));
    //     }

    //     System.out.println("Nstm:");
    //     printBB(board.getNstmAttacks());

    //     System.out.println("");
    //     printBB(board.getBlockers());

    //     printBB(board.getCheckers());


    // }

}