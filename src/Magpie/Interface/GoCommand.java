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
                    limit.searchmoves = Engine.MoveList.legal(_state.board.getAs(), moves, _state.board.get().getMoveDecoder());
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
                    if (i < args.length)
                        limit.capturesOnly = Boolean.parseBoolean(args[i+1]);
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
        Engine.Board board = _state.board.getAs();
        Engine.SearchLimit limit = params_get("limit");
        if (params_get("perft") != null) {

            // execute perft
            long c = board.perft(limit.depth, (move, count) -> {
                new TextResponse(Engine.Move.toString(move) + ": " + count).send();
            }, limit);
            new TextResponse("Nodes searched: " + c).send();

        } else if (!(params_get("ponder") == null)) {

            // execute ponder

        } else {

            // execute search
            Engine.AlphaBetaSearchTree search = new Engine.AlphaBetaSearchTree(board);
            _state.search.set(search);

            search.onNewIDIteration.register(su -> {
                new InfoResponse.Builder()
                    .depth(su.depth)
                    .seldepth(su.seldepth)
                    .multipv(1) // TODO: add multipv
                    .score(su.eval, ScoreType.CentiPawns) // TODO: handle other score types
                    .nodes(su.nodes)
                    .nps(su.nps)
                    .time(su.time)
                    .pv(su.pvline, board.getMoveEncoder())
                    .build()
                    .send(); 
            });
            search.onSearchStopped.register(sr -> {
                new BestMoveResponse(
                    Engine.Move.toString(sr.bestMove), 
                    sr.ponderMove == Engine.Move.None ? null : Engine.Move.toString(sr.ponderMove))
                    .send();
            });
            search.onNewRootMoveSearch.register(su -> {
                new InfoResponse.Builder()
                    .depth(su.depth)
                    .currmove(su.currmove, board.getMoveEncoder())
                    .currmovenumber(su.currmovenumber)
                    .build()
                    .send();
            });

            search.begin(limit);
        }
    }

}
