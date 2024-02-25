package Interface.Custom;

import Engine.IBoard;

/*
 * piece [get <square> | add <square> <piece> | remove <square> ]
 */
public abstract class PieceCommand extends Command {

    protected int _square;

    public PieceCommand(IBoard board, int square) {
        super(board);
        _square = square;
    }
}
