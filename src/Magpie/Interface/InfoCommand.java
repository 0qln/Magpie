package Interface;

import static Engine.Utils.*;

import Engine.Color;
import Engine.StaticEvaluator;

public class InfoCommand extends Command {

    static {
        Signature.register("info", InfoCommand.class, new Builder<>(() -> new InfoCommand()));
    }

    @Override
    public boolean parseArgs(String[] args) {
        if (args.length < 1) {
            return false;
        }

        for (int i = 0; i < args.length; i++)
            params_put(args[i], true);

        return true;
    }

    @Override
    public void run() {
        Engine.Board board = _state.board.getAs();
        
        if (params_getB("eval")) {
            new TextResponse(new StaticEvaluator(board).evaluate(Engine.Color.White)).send();
        }

        if (params_getB("castling")) {
            new TextResponse(Engine.Castling.toString(board.getCastling())).send();
        }

        if (params_getB("check")) {
            new TextResponse("Checkers: ").send();
            long[] checkers = { board.getCheckers() };
            new BitboardResponse(checkers).send();
            while (checkers[0] != 0)
                new SquareInfoResponse(popLsb(checkers), "Checker").send();

            new TextResponse("Not-side-to-move attacks: ").send();
            new BitboardResponse(board.getNstmAttacks()).send();

            new TextResponse("Check blockers: ").send();
            new BitboardResponse(board.getBlockers()).send();
        }

        if (params_getB("phase")) {
            new TextResponse("Phase: " + StaticEvaluator.phase(board) + "/32").send();
        }

        if (params_getB("bitboards")) {
            new TextResponse("White pieces: ").send();
            new BitboardResponse(board.getCBitboard(Color.White)).send();

            new TextResponse("Black pieces: ").send();
            new BitboardResponse(board.getCBitboard(Color.Black)).send();
        }
    }

}
