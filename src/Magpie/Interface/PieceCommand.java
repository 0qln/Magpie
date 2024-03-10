package Interface;

import java.util.ArrayList;
import java.util.Arrays;

import Engine.Piece;
import Interface.Command.*;
import Misc.Ptr;

/*
 * piece [get <square0> ... <squaren> | add <square> <piece> | remove <square0> ... <squaren>  ]
 */
public class PieceCommand extends Command {

    static {
        Signature.register("piece", PieceCommand.class, new Builder<>(() -> new PieceCommand()));
    }

    @Override
    public boolean parseArgs(String[] args) {
        if (args.length < 2) {
            return false;
        }

        switch (args[0]) {
            case "add":
                params_put("piece", Piece.fromChar(args[2].charAt(0)));
                params_put(args[0], Misc.Utils.toSquareIndex(args[1]));
                break;
            case "remove":
            case "get":
                params_put(args[0], Misc.Utils.select(
                        Arrays.copyOfRange(args, 1, args.length),
                        square -> Misc.Utils.toSquareIndex(square)));
                break;
        }

        return true;
    }

    @Override
    public void run() {
        if (params_get("add") != null) {
            _state.board.get().addPiece(params_get("add"), params_get("piece"));
        }
        if (params_get("remove") != null) {
            ArrayList<Integer> a = params_get("remove");
            for (Integer square : a)
                _state.board.get().removePiece(square);
        }
        if (params_get("get") != null) {
            ArrayList<Integer> a = params_get("remove");
            for (Integer square : a)
                new SquareInfoResponse(square, "" + Piece.toChar(_state.board.get().getPieceID(square))).send();
        }
    }

}
