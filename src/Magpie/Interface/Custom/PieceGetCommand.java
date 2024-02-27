package Interface.Custom;

import Engine.Piece;
import Misc.Ptr;

public class PieceGetCommand extends PieceCommand {

    public PieceGetCommand(Ptr<Engine.IBoard> board, int square) {
        super(board, square);
    }
    
    public void run() {
        new SquareInfoResponse(_square, Character.toString(
            Piece.toChar(_board.get().getPiece(_square)))).send();
    }
}
