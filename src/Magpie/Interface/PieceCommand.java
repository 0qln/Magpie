package Interface;

import Engine.PieceUtil;
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
        if (args.length < 2) {
            return false;
        }

        switch (args[0]) {
            case "add":
                params_put("piece", PieceUtil.fromChar(args[2].charAt(0)));
            case "remove":
            case "get":
                params_put(args[0], Misc.Utils.toSquareIndex(args[1]));
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
            _state.board.get().removePiece(params_get("remove"));
        }
        if (params_get("get") != null) {
            int sq = params_get("get");
            new SquareInfoResponse(sq, ""+PieceUtil.toChar(_state.board.get().getPieceID(sq))).send();
        }
    }

}
