package Interface.Custom;

import Misc.Ptr;

/*
 * piece [get <square> | add <square> <piece> | remove <square> ]
 */
public abstract class PieceCommand extends Command {

    protected int _square;

    public PieceCommand(Ptr<Engine.IBoard> board, int square) {
        super(board);
        _square = square;
    }
}
