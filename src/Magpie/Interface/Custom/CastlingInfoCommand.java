package Interface.Custom;

import Engine.IBoard;
import Misc.Ptr;

public class CastlingInfoCommand extends InfoCommand {

    public CastlingInfoCommand(Ptr<IBoard> board) {
        super(board);
    }

    @Override
    public void run() {
        Engine.Board board = _board.getAs();
        System.out.println(Engine.Castling.toString(board.getCastling()));
    }

}
