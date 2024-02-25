package Interface.Custom;

import Engine.IBoard;

public class PieceGetCommand extends PieceCommand {

    public PieceGetCommand(IBoard board, int square) {
        super(board, square);
    }
    
    public void run() {
        
    }
}
