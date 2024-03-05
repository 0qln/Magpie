package Interface;

import java.util.Arrays;

public class PositionCommand extends Command
{
    private static final String[] FEN_STARTPOS = 
        new String[] { "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR", "w", "KQkq", "-", "0", "1" };

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
        // Set up position
        _board.set(_board.get()
            .getBuilder()
            .fen(params_get("fen"))
            .build());

        // Play moves
        String[] moves = params_get("moves");
        for (String moveStr : moves) {
            short move = _board.get().getMoveDecoder().decode(moveStr);
            _board.get().makeMove(move);
        }
    }

}
