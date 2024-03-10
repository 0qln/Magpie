package Engine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import Misc.Ptr;

public class AlphaBetaSearchTree extends ISearchTree {

    private final Event.Dispatcher<SearchUpdate> onNewRootMoveSearchDispatcher = new Event.Dispatcher<>();
    public final Event<SearchUpdate> onNewRootMoveSearch = new Event<>(onNewRootMoveSearchDispatcher);

    private final Event.Dispatcher<SearchResult> onSearchStoppedDispatcher = new Event.Dispatcher<>();
    public final Event<SearchResult> onSearchStopped = new Event<>(onSearchStoppedDispatcher);

    private final Event.Dispatcher<SearchUpdate> onNewIDIterationDispatcher = new Event.Dispatcher<>();
    public final Event<SearchUpdate> onNewIDIteration = new Event<>(onNewIDIterationDispatcher);

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
    private long _startTime, _timePerMove;
    private long _maxNodes = -1;

    public AlphaBetaSearchTree(Board board) {
        this._board = board;
        this._rootMoves = MoveList.legal(board, false);
        this._rootScores = new int[_rootMoves.length()];
        Arrays.fill(_rootScores, -StaticEvaluator.Infinity);
        this._nodesSearched = 0;
        this._infoStack = new ArrayList<>();
    }

    @Override
    public void begin(SearchLimit limit) {
        _logger.info("Begin search.");
        _stopFlag = false;

        // Initiate time limit
        _startTime = System.nanoTime();
        long inc = _board.getTurn() == Color.White ? limit.winc : limit.binc;
        long time = _board.getTurn() == Color.White ? limit.wtime : limit.btime;
        _timePerMove = limit.movetime != -1 ? limit.movetime * 100 : 
                time != -1 
                        ? (time / 30 + inc) * 1000000 
                        : -1;

        _logger.info("Time per move: " + _timePerMove);

        // Initiate search space limit
        _maxNodes = limit.nodes;

        // iterative deepening loop
        for (_rootDepth = 1; _rootDepth <= limit.depth; _rootDepth++) {

            _logger.info("New root iteration (depth: " + _rootDepth + ")");

            // Ensure info objs.
            // Because of the quiescent search extension, it is possible that there are
            // already anough info objs.
            if (_infoStack.size() < _rootDepth)
                _infoStack.add(new DepthLevelInfo());

            long beginTime = System.nanoTime();

            Ptr<Line> pv = new Ptr<Line>(null);
            Ptr<Line> rightline = new Ptr<Line>(null);
            try {
                rootNode(_rootDepth, -StaticEvaluator.Infinity, StaticEvaluator.Infinity, pv, rightline);
            } catch (Exception e) {
                _logger.log(Level.SEVERE, "Exception during search: ", e);
                _logger.severe("Root Depth: " + _rootDepth);
                _logger.severe("Line: " + rightline.get().toString());
                stop();
            }

            // If the search didn't finish, it cannot be trusted and should be discarded.
            if (_stopFlag) {
                // We have to ensure that there is atleast some pv move that can be returned if
                // the first search didn't finish.
                if (_rootDepth == 1) {
                    _pv = pv.get();
                    _rootMoves.sort(_rootScores);
                }

                // Stop the iterative deepening search.
                break;
            }

            // Prepare for next iteration.
            _pv = pv.get();
            _rootMoves.sort(_rootScores);

            // Call each callback
            final double elapsedMilliseconds = (System.nanoTime() - beginTime) / 1e6;
            final double elapsedSeconds = elapsedMilliseconds / 1e3;
            onNewIDIterationDispatcher.dispatch(() -> new SearchUpdate(
                    _rootScores[0] * (_board.getTurn() * -2 + 1),
                    _rootDepth,
                    _rootSelDepth,
                    _nodesSearched,
                    generatePv(),
                    (long) (_nodesSearched / elapsedSeconds),
                    (long) elapsedMilliseconds));
        }

        _logger.info("Search finished.");

        _stop();
    }

    private void checkTime() {
        long currentTime = System.nanoTime();
        long elapsedTime = currentTime - _startTime;
        if (_timePerMove != -1 && elapsedTime >= _timePerMove) {
            stop();
        }
    }

    private void checkSearchSpace() {
        if (_maxNodes != -1 && _nodesSearched >= _maxNodes) {
            stop();
        }
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
        onSearchStoppedDispatcher.dispatch(() -> new SearchResult(
                // The best move is the pv move at our depth
                pv[0],
                // Ponder on the best response of the enemy
                pv.length > 1 ? pv[1] : Move.None));
    }

    private void rootNode(int depth, int alpha, int beta, Ptr<Line> pv, Ptr<Line> currline) {
        int bestScore = -StaticEvaluator.Infinity;

        for (int i = 0; i < _rootMoves.length(); i++) {
            final short move = _rootMoves.get(i);

            Line line = new Line(move);
            Line cline = new Line(move);
            currline.set(cline);

            _board.makeMove(move);
            _rootScores[i] = -search(depth - 1, alpha, beta, line, cline, 1);
            _board.undoMove(move);

            // Too much traffic for UCI on lower depths
            // TODO: make this relative on the time the last iteration took
            final int moveIndex = i;
            if (_rootDepth >= 6)
                onNewRootMoveSearchDispatcher.dispatch(() -> new SearchUpdate(
                        _rootDepth,
                        move,
                        moveIndex));

            if (_rootScores[i] >= bestScore) {
                bestScore = _rootScores[i];
                pv.set(line);
            }
        }
    }

    private int search(int depth, int alpha, int beta, Line parentPV, Line currline, int ply) {
        // Increment node counter.
        _nodesSearched++;

        // Check limits
        checkTime();
        checkSearchSpace();

        if (_stopFlag)
            return 0;

        // Three fold repitition
        if (_board.hasThreeFoldRepitition())
            return StaticEvaluator.Draw;
        
        if (depth <= 0)
            return quiescent(alpha, beta, ply);

        // Initiate node vars
        int bestScore = -StaticEvaluator.Infinity, score = bestScore;
        MoveList moves = MoveList.legal(_board, false);
        // The iterative deepening loop ensures there is one for the target depth.
        DepthLevelInfo info = _infoStack.get(ply);

        // Check for a game-over
        if (moves.length() == 0) {
            // Terminal node, Game over
            if (_board.isInCheck()) {
                // We have no more moves and are in check
                // => We are checkmated
                return -StaticEvaluator.Checkmate
                        // To promote the earliest checkmate, add
                        // a bonus for shallower mates.
                        - depth;
            }
            // We have no more moves, but are not in check
            // => Stalemate
            return StaticEvaluator.Draw;
        }

        // Sort the moves for increased chance of cutoff
        sort(moves, info);

        // Loop through all child nodes.
        for (int i = 0; i < moves.length(); i++) {
            short move = moves.get(i);
            Line line = new Line(move);
            Line cline = new Line(move);
            currline.update(cline);

            // Make move
            _board.makeMove(move);

            // Evaluate all nodes from the opponents perspective.
            score = -search(depth - 1, -beta, -alpha, line, cline, ply + 1);

            // Undo move
            _board.undoMove(move);

            // Simple beta cutoff (fail-hard)
            if (score >= beta) {
                return beta;
            }

            // Update alpha
            if (score > alpha) {
                alpha = score;
            }

            // Update best score
            if (score > bestScore) {
                bestScore = score;
                parentPV.update(line);
            }
        }

        // Return the best score.
        return bestScore;
    }

    private int quiescent(int alpha, int beta, int ply) {
        // Increment node counter.
        _nodesSearched++;

        // Raise the selective depth to the maximum depth.
        _rootSelDepth = Math.max(ply, _rootSelDepth);

        // Check limits
        checkTime();
        checkSearchSpace();

        if (_stopFlag) {
            return 0;
        }

        // Because of the Null Move Observation we can assume that doing nothing will
        // always be worse than doing some capture.
        // => Use the static evaluation as a lower bound for this node.
        int score = StaticEvaluator.evaluate(_board, _board.getTurn());

        // If the static evaluation of this position is already better or equal to the
        // last known best capture of a sibling node.
        // => Prune this node early.
        if (score >= beta)
            // Return beta (fail-hard)
            return beta;

        // Raise the lower bound, alpha, to the static evaluation. We are only concerned
        // about moves that will create a better position.
        if (alpha < score)
            alpha = score;

        // Get all the possible captures from this position.
        MoveList moves = MoveList.legal(_board, true);

        // Get info obj. Generate a new info obj if is none for this ply.
        while (ply >= _infoStack.size())
            _infoStack.add(new DepthLevelInfo());
        DepthLevelInfo info = _infoStack.get(ply);

        // Sort moves
        sort(moves, info);

        // Play out all captures. We want a quiet position to evaluate.
        for (int i = 0; i < moves.length(); i++) {
            short move = moves.get(i);

            // Make move
            _board.makeMove(move);

            // Quiscent evaluation.
            score = -quiescent(-beta, -alpha, ply + 1);

            // Undo move
            _board.undoMove(move);

            // Simple alpha-beta cutoff (fail-hard)
            if (score >= beta)
                return beta;

            // Raise alpha (and update PV?)
            if (score > alpha) {
                alpha = score;
            }
        }

        // We are interested in tactical moves that increase alpha, if the moves that
        // lead resulted in either [ the static eval of this position | the tactical
        // captures that are possible from this position ] are worse than alpha, we can
        // thus simply return alpha, otherwise alpha will have been raised earlier in
        // this function to the better evaluation.
        return alpha;
    }

    private void sort(MoveList moves, DepthLevelInfo info) {
        int[] scores = new int[moves.length()];

        for (int i = 0; i < scores.length; i++) {
            short move = moves.get(i);

            // TODO: killer moves

            // MVV-LVA
            if (Move.isCapture(Move.getFlag(move))) {
                int victimType = Piece.getType(_board.getPieceID(Move.getTo(move)));
                int agressorType = Piece.getType(_board.getPieceID(Move.getFrom(move)));
                scores[i] = 100 * victimType - agressorType;
            }
        }

        moves.sort(scores);
    }

    public static class Line {
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
