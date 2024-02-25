package Interface.Custom;

import Engine.IBoard;
import Engine.Piece;

public class PieceGetCommand extends PieceCommand {

    public PieceGetCommand(IBoard board, int square) {
        super(board, square);
    }
    
    public void run() {
        new SquareInfoResponse(_square, Character.toString(Piece.toChar(_board.getPiece(_square)))).send();
    }
}
