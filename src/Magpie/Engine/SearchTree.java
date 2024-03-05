package Engine;

import java.util.ArrayList;
import java.util.function.Consumer;

public class SearchTree {

    private class AspirationWindow {
        private int _lowerBound, _upperBound;

        public AspirationWindow(int bounds) {
            _lowerBound = -bounds;
            _upperBound = bounds;
        }

        public AspirationWindow(int lower, int upper) {
            _lowerBound = lower;
            _upperBound = upper;
        }

        public int getLowerBound() {
            return _lowerBound;
        }

        public int getUpperBound() {
            return _upperBound;
        }

        public void widenUpper() {

        }

        public void widenLower() {

        }
    }

    // On each search result, iterate these and distribute the result of the search.
    public ArrayList<Consumer<SearchResult>> Callbacks;

    private Board _board;

    private StaticEvaluator _staticEval;

    private AspirationWindow _rootWindow;
    private int _rootDepth;
    private MoveList _rootMoves;

    public SearchTree(Board board) {
        this._board = board;
        this._rootMoves = MoveList.legal(board);
        this._staticEval = new StaticEvaluator(board);
        this._rootWindow = new AspirationWindow(StaticEvaluator.Infinity);
    }

    public void begin(int targetDepth) {

        for (_rootDepth = 1; _rootDepth <= targetDepth; _rootDepth++) {
            // new RootSearch(_rootDepth);
        }

    }

    public void stop() {

    }

    public void ponder(int targetDepth) {

    }

    @FunctionalInterface
    public interface ISearch {

        public int eval(int depth, int alpha, int beta);

    }

    private abstract class SearchBase {

        // Search function templates

    }

    // public class ScoutSearch extends SearchBase implements ISearch {

    //     // Zero window search

    //     @Override
    //     public int eval() {
    //         return 0;
    //     }

    // }

    // public class PvSearch extends SearchBase implements ISearch {

    //     //

    // }

    // public class RootSearch extends SearchBase implements ISearch {

    //     private int _targetDepth;

    //     public RootSearch(int targetDepth) {
    //         _targetDepth = targetDepth;
    //     }

    //     @Override
    //     public int eval(int depth, int alpha, int beta) {
    //         ISearch search = new PvSearch();

    //         for (short move : _rootMoves.getMoves()) {
    //             _board.makeMove(move);
    //             int value = -search.eval(
    //                     _targetDepth,
    //                     _rootWindow.getLowerBound(),
    //                     _rootWindow.getUpperBound());
    //             _board.undoMove(move);
    //         }
    //     }

    // }

    // public class QuiescenceSearch extends SearchBase implements ISearch {

    //     //

    // }

}
