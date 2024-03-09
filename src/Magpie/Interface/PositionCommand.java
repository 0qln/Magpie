package Interface;

import java.util.Arrays;

public class PositionCommand extends Command {
    private static class Position {
        public Engine.IBoard board;
        public String[] fen;
        public String[] moves;
    }

    private static Position _lastPosition;

    public PositionCommand() {
        _forceSync = true;
    }

    private static final String[] FEN_STARTPOS = new String[] { "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR", "w", "KQkq", "-", "0", "1" };

    static {
        Signature.register("position", PositionCommand.class, new Builder<>(() -> new PositionCommand()));
    }

    @Override
    public boolean parseArgs(String[] args) {
        if (args.length < 1) {
            return false;
        }
        final int movesidx = Misc.Utils.indexOf(args, e -> e.equals("moves"));
        params_put("fen", args[0].equals("startpos")
                ? FEN_STARTPOS
                : Arrays.copyOfRange(args, 1, movesidx == -1 ? args.length : movesidx));
        params_put("moves", movesidx == -1
                ? new String[0]
                : Arrays.copyOfRange(args, movesidx + 1, args.length));
        return true;
    }

    public void run() {
        String[] moves = params_get("moves");
        String[] fen = params_get("fen");

        // For quick position set up during games
        if (!_state.board.isNull() &&
                _lastPosition != null &&
                _state.board.get() == _lastPosition.board &&
                moves.length > 0 &&
                Arrays.equals(_lastPosition.fen, fen) &&
                Arrays.equals(
                        _lastPosition.moves, 0, _lastPosition.moves.length,
                        moves, 0, _lastPosition.moves.length)
                && (
                    _lastPosition.moves.length == moves.length - 2 ||
                    _lastPosition.moves.length == moves.length - 1
                )) {

            // Only make the last moves that were added
            for (int i = _lastPosition.moves.length; i < moves.length; i++)
                _state.board.get().makeMove(_state.board.get().getMoveDecoder().decode(moves[i]));

        } else {
            
            // Set up position
            _state.board.set(_state.board.get()
                    .getBuilder()
                    .fen(fen)
                    .build());

            // Play moves
            for (String moveStr : moves) {
                short move = _state.board.get().getMoveDecoder().decode(moveStr);
                _state.board.get().makeMove(move);
            }
        }

        _lastPosition = new Position();
        _lastPosition.board = _state.board.get();
        _lastPosition.fen = fen;
        _lastPosition.moves = moves;
    }

}
