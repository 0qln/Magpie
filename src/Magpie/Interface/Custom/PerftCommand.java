package Interface.Custom;

import java.util.Optional;

import Engine.IBoard;
import Misc.Ptr;

public class PerftCommand extends Command {

    private int _depth;

    public PerftCommand(Ptr<IBoard> board, Optional<Integer> depth) {
        super(board);
        _depth = depth.orElse(1);
    }

    @Override
    public void run() {
        Engine.Board board;
        if (_board.get() instanceof Engine.Board) {
            board = (Engine.Board)_board.get();
        }
        else {
            return;
        }
        board.perft(_depth, true);
    }
    
}
