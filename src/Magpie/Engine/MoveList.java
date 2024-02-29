package Engine;

import java.util.Arrays;

public class MoveList {
    private final short[] _moves = new short[256];
    private int _moveCount = 0;


    public static MoveList generate(Board board) {
        MoveList list = new MoveList();

        // Generate using generator classes
        // list._moveCount = new PawnMoveGenerator().generate(list._moves, list._moveCount, board, board.getTurn());
        // list._moveCount = new KnightMoveGenerator().generate(list._moves, list._moveCount, board, board.getTurn());
        // list._moveCount = new RookMoveGenerator().generate(list._moves, list._moveCount, board, board.getTurn());        
        list._moveCount = new BishopMoveGenerator().generate(list._moves, list._moveCount, board, board.getTurn());        

        return list;
    }

    public short[] getMoves() {
        // TODO? Remove overhead caused by copying
        return Arrays.copyOfRange(_moves, 0, _moveCount);
    }
}
