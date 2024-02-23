package Interface.Custom;

import Engine.IBoard;

public class PieceRemoveCommand extends PieceCommand {

    public PieceRemoveCommand(IBoard board, int square) {
        super(board, square);
    }

    @Override
    public void run() {
        _board.removePiece(_square);
    }
}
