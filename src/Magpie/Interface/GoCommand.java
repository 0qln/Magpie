package Interface;

import java.util.Arrays;

public class GoCommand extends Command {

    static {
        Signature.register("go", GoCommand.class, new Builder<>(() -> new GoCommand()));
    }

    @Override
    public boolean parseArgs(String[] args) {
        Engine.SearchLimit limit = new Engine.SearchLimit();
        for (int i = 0; i < args.length; i++) {
            final String currentToken = args[i];
            final String nextToken = ++i < args.length ? args[i] : null;
            switch (currentToken) {
                // Has to be last
                case "searchmoves":
                    String[] moves = Arrays.copyOfRange(args, ++i, args.length);
                    limit.searchmoves = Engine.MoveList.legal(_board.getAs(), moves, _board.get().getMoveDecoder());
                    params_put("searchmoves", moves);
                    break;

                case "wtime":
                    limit.wtime = Long.parseLong(nextToken);
                    break;
                case "btime":
                    limit.btime = Long.parseLong(nextToken);
                    break;
                case "winc":
                    limit.winc = Long.parseLong(nextToken);
                    break;
                case "binc":
                    limit.binc = Long.parseLong(nextToken);
                    break;
                case "nodes":
                    limit.nodes = Long.parseLong(nextToken);
                    break;
                case "movetime":
                    limit.movetime = Long.parseLong(nextToken);
                    break;

                case "movestogo":
                    limit.movestogo = Integer.parseInt(nextToken);
                    break;
                case "depth":
                    limit.depth = Integer.parseInt(nextToken);
                    break;
                case "mate":
                    limit.mate = Integer.parseInt(nextToken);
                    break;

                case "perft":
                    limit.depth = Integer.parseInt(nextToken);
                case "infinite":
                case "ponder":
                    params_put(currentToken, true);
                    break;

                default:
                    break;
            }
        }
        params_put("limit", limit);
        return true;
    }

    @Override
    public void run() {
        Engine.Board board = _board.getAs();
        Engine.SearchLimit limit = params_get("limit");
        if (params_get("perft") != null) {

            // execute perft
            long c = board.perft(limit.depth, (move, count) -> {
                new TextResponse(Engine.Move.toString(move) + ": " + count).send();
            });
            new TextResponse("Nodes searched: " + c).send();

        } else if (!(params_get("ponder") == null)) {

            // execute ponder

        } else {

            // execute search
            Engine.AlphaBetaSearchTree search = new Engine.AlphaBetaSearchTree(board);
            search.Callbacks.add(sr -> {
                new TextResponse(sr.depth).send();
            });
            search.begin(limit);

        }
    }

}
