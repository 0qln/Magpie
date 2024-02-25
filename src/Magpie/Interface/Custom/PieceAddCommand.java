package Interface.Custom;

import Engine.IBoard;

public class PieceAddCommand extends PieceCommand {
    private int _piece;

    public PieceAddCommand(IBoard board, int square, int piece) {
        super(board, square);
        _piece = piece;
    }
    
    public void run() {
        _board.addPiece(_square, _piece);
    }
}
