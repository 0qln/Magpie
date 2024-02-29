package Engine;

public class QueenMoveGenerator extends MoveGenerator {

    @Override
    int generate(short[] list, int index, Board board, int color) {
        var r = new RookMoveGenerator();
        var b = new BishopMoveGenerator();
        index = r.generateCaptures(list, index, board, color);
        index = b.generateCaptures(list, index, board, color);
        index = r.generateQuiets(list, index, board, color);
        index = b.generateQuiets(list, index, board, color);
        return index;
    }


}
