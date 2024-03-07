package Engine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import Misc.Ptr;

public class AlphaBetaSearchTree extends ISearchTree {

    // On each search result, iterate these and distribute the result of the search.
    public List<Consumer<SearchUpdate>> CallbacksOnIter = new ArrayList<Consumer<SearchUpdate>>();
    public List<Consumer<SearchResult>> CallbacksOnStop = new ArrayList<Consumer<SearchResult>>();

    private StaticEvaluator _staticEval;
    private final Board _board;
    private final MoveList _rootMoves;
    private final int[] _rootScores;
    // This needs to be indexed alot, so a linked list would be inefficient.
    private final ArrayList<DepthLevelInfo> _infoStack;
    private long _nodesSearched;
    private int _rootDepth, _rootSelDepth;
    private Logger _logger = Misc.LoggerConfigurator.configureLogger(AlphaBetaSearchTree.class);
    private Line _pv;
    private boolean _stopFlag;

    public AlphaBetaSearchTree(Board board) {
        this._board = board;
        this._staticEval = new StaticEvaluator(board);
        this._rootMoves = MoveList.legal(board);
        this._rootScores = new int[_rootMoves.length()];
        Arrays.fill(_rootScores, -StaticEvaluator.Infinity);
        this._nodesSearched = 0;
        this._infoStack = new ArrayList<>();
    }

    @Override
    public void begin(SearchLimit limit) {
        
        _logger.info("Begin search.");

        _stopFlag = false;
        
        // iterative deepening loop
        for (_rootDepth = 1; _rootDepth <= limit.depth; _rootDepth++) {

            _logger.info("New root iteration (depth: " + _rootDepth + ")");

            // Create the new level info
            // TODO: handle unknown extension length of quies search
            _infoStack.add(new DepthLevelInfo());

            Ptr<Line> pv = new Ptr<Line>(null);
            try {
                rootNode(_rootDepth, -StaticEvaluator.Infinity, StaticEvaluator.Infinity, pv);
            }
            catch (Exception e) {
                _logger.log(Level.SEVERE, "Exception during search: ", e);
                _logger.severe("Root Depth: " + _rootDepth);
                _logger.severe("Line: " + pv.get().toString());
                stop();
            }

            // If the search didn't finish, it cannot be trusted and should be discarded.
            if (_stopFlag) {
                break;
            }
            
            _pv = pv.get();
            
            _rootMoves.sort(_rootScores);

            SearchUpdate sr = new SearchUpdate(
                    _rootScores[0] * (_board.getTurn() * -2 + 1),
                    _rootDepth,
                    _rootDepth,
                    _nodesSearched,
                    generatePv());

            for (Consumer<SearchUpdate> callback : CallbacksOnIter) {
                callback.accept(sr);
            }
        }
        
        _logger.info("Search finished.");

        _stop();
    }

    private short[] generatePv() {
        ArrayList<Short> list = new ArrayList<>();
        list.add(_pv.getMove());
        Line node = _pv;
        while (node.hasNext()) {
            node = node.getNext();
            list.add(node.getMove());
        }
        short[] result = new short[list.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = list.get(i);
        }
        return result;
    }

    @Override
    public void stop() {
        _logger.info("Stop requested.");
        
        _stopFlag = true;
    }
    
    private void _stop() {
        _logger.info("Stop search.");
        
        // Distribute results
        short[] pv = generatePv();
        for (Consumer<SearchResult> callback : CallbacksOnStop) {
            callback.accept(new SearchResult(
                    // The best move is the pv move at our depth
                    pv[0],
                    // Ponder on the best response of the enemy
                    pv.length > 1 ? pv[1] : Move.None));
        }
    }

    private void rootNode(int depth, int alpha, int beta, Ptr<Line> pv) {
        int bestScore = -StaticEvaluator.Infinity;

        for (int i = 0; i < _rootMoves.length(); i++) {
            short move = _rootMoves.get(i);
            Line line = new Line(move);
            _board.makeMove(move);
            _rootScores[i] = -search(depth - 1, alpha, beta, line);
            _board.undoMove(move);

            // if (_rootScores[i] >= beta) {
                // break;
            // }

            // if (_rootScores[i] > alpha) {
                // alpha = _rootScores[i];
            // }

            if (_rootScores[i] >= bestScore) {
                bestScore = _rootScores[i];
                pv.set(line);
            }
        }
    }

    private int search(int depth, int alpha, int beta, Line parentPV) {
        _nodesSearched++;
        
        if (_stopFlag) {
            return 0;
        }

        if (depth <= 0) {
            // Return static eval relative to stm.
            return _staticEval.evaluate(_board.getTurn());
        }

        int bestScore = -StaticEvaluator.Infinity, score = bestScore;
        MoveList moves = MoveList.legal(_board);

        if (moves.length() == 0) {
            // Terminal node, Game over
            if (_board.isInCheck()) {
                // We have no more moves and are in check
                // => We are checkmated
                return -StaticEvaluator.Checkmate
                        // To promote the earliest checkmate, add
                        // a bonus for shallower mates.
                        // - depth
                        ;
            }
            // We have no more moves, but are not in check
            // => Stalemate
            return StaticEvaluator.Draw;
        }

        DepthLevelInfo info = _infoStack.get(_rootDepth - depth);

        // Sort the moves
        sort(moves, info);

        for (int i = 0; i < moves.length(); i++) {            
            short move = moves.get(i);
            Line line = new Line(move);
            _board.makeMove(move);
            score = -search(depth - 1, -beta, -alpha, line);
            _board.undoMove(move);

            // if (score >= beta) {
                // return beta;
            // }

            // if (score > alpha) {
                // alpha = score;
            // }

            if (score > bestScore) {
                bestScore = score;
                parentPV.update(line);
            }
        }

        return bestScore;
    }

    private void sort(MoveList moves, DepthLevelInfo info) {
        int[] scores = new int[moves.length()];

        for (int i = 0; i < scores.length; i++) {
            short move = moves.get(i);

            // MVV-LVA
            if (Move.isCapture(move)) {
                int victimType = PieceUtil.getType(_board.getPieceID(Move.getTo(move)));
                int agressorType = PieceUtil.getType(_board.getPieceID(Move.getFrom(move)));
                scores[i] = 100 * victimType - agressorType;
            }
        }

        moves.sort(scores);
    }

    private static class Line {
        public short _move;
        public Line _child = null;

        public Line(short move) {
            _move = move;
        }

        public void update(Line nextNode) {
            _child = nextNode;
        }

        public boolean hasNext() {
            return _child != null;
        }

        public Line getNext() {
            return _child;
        }

        public short getMove() {
            return _move;
        }

        @Override
        public String toString() {
            StringBuilder result = new StringBuilder(Move.toString(_move));
            Line curr = this;
            while (curr.hasNext()) {
                curr = curr._child;
                result.append(' ').append(Move.toString(curr._move));
            }
            return result.toString();
        }
    }
}
