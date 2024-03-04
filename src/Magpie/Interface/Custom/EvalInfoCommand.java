package Interface.Custom;

import Engine.IBoard;
import Engine.StaticEvaluator;
import Misc.Ptr;

public class EvalInfoCommand extends InfoCommand {

    public EvalInfoCommand(Ptr<IBoard> board) {
        super(board);
    }

    @Override
    public void run() {
        Engine.Board board = _board.getAs();
        new Response(
            Integer.toString( new StaticEvaluator(board)
                .evaluate(Engine.Color.White))
        ).send();
    }

}
