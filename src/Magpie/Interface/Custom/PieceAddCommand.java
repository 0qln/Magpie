package Interface.Custom;

import Misc.Ptr;

public class PieceAddCommand extends PieceCommand {
    private int _piece;

    public PieceAddCommand(Ptr<Engine.IBoard> board, int square, int piece) {
        super(board, square);
        _piece = piece;
    }
    
    public void run() {
        _board.get().addPiece(_square, _piece);
    }
}
