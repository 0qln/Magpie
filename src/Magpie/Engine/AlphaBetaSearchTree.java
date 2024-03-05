package Engine;

import java.util.ArrayList;
import java.util.function.Consumer;

public class AlphaBetaSearchTree {

    // On each search result, iterate these and distribute the result of the search.
    public ArrayList<Consumer<SearchResult>> Callbacks;

    private StaticEvaluator _staticEval;
    private final Board _board;
    private final MoveList _rootMoves;
    private final int[] _rootScores;
    private int _rootDepth;

    public AlphaBetaSearchTree(Board board) {
        this._board = board;
        this._staticEval = new StaticEvaluator(board);
        this._rootMoves = MoveList.legal(board);
        this._rootScores = new int[_rootMoves.length()];
    }

    public void begin(int targetDepth) {

        // iterative deepening loop

        for (_rootDepth = 1; _rootDepth <= targetDepth; _rootDepth++) {

            rootNode(_rootDepth, -StaticEvaluator.Infinity, StaticEvaluator.Infinity);

            _rootMoves.sort(_rootScores);

            SearchResult sr = new SearchResult(
               _rootScores[0], 
               _rootDepth, 
               _rootDepth
            );

            for (Consumer<SearchResult> callback : Callbacks) {
                callback.accept(sr);
            }
        }

    }

    private int rootNode(int depth, int alpha, int beta) {

        for (int i = 0; i < _rootMoves.length(); i++) {
            short move = _rootMoves.get(i);
            _board.makeMove(move);
            _rootScores[i] = -search(depth, alpha, beta);
            _board.undoMove(move);
        }

        // This would normally return the evaluation of the position, but we already
        // store this in the `root` fields.
        return 0;
    }

    private int search(int depth, int alpha, int beta) {
        if (depth <= 0) {
            return _staticEval.evaluate(_board.getTurn());
        }

        int best = -StaticEvaluator.Infinity;
        MoveList moves = MoveList.legal(_board);

        for (int i = 0; i < moves.length(); i++) {
            short move = moves.get(i);
            _board.makeMove(move);
            int score = -search(depth-1, -beta, -alpha);

            if (score >= beta) {
                return score;
            }

            if (score > best) {
                best = score;

                if (score > alpha) {
                    alpha = score;
                }
            }
        }

        return best;
    }

}
