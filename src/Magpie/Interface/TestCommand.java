package Interface;

import java.util.Random;
import Engine.Zobrist;

public class TestCommand extends Command {

    static {
        Signature.register("test", TestCommand.class, new Builder<>(() -> new TestCommand()));
    }

    @Override
    public boolean parseArgs(String[] args) {
        for (int i = 0; i < args.length; i++)
            params_put(args[i], true);
        return true;
    }

    @Override
    public void run() {
        if (params_getB("zobrist")) {
            // find a random seed with minimal collisions
            var rng = new Random();
            var collisionsMin = Long.MAX_VALUE;
            while (true) {
                long seed = rng.nextLong();
                Zobrist.init(seed);
                // TODO: measure collisions
                long collisions = 0;
                if (collisions < collisionsMin) {
                    collisionsMin = collisions;
                    new TextResponse("New seed: " + seed + (" (" + collisions + " collisions)")).send();
                }
            }
        }
        if (params_getB("eret")) {
            //https://www.chessprogramming.org/Eigenmann_Rapid_Engine_Test
        }
        if (params_getB("movegen")) {
            new TextResponse("Position 1 success: " +
                    testPosition("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", 5, 4865609L))
                    .send();

            new TextResponse("Position 2 success: " +
                    testPosition("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1", 5, 193690690L))
                    .send();

            new TextResponse("Position 3 success: " +
                    testPosition("8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - - 0 1", 6, 11030083L))
                    .send();

            new TextResponse("Position 4 success: " +
                    testPosition("r3k2r/Pppp1ppp/1b3nbN/nP6/BBP1P3/q4N2/Pp1P2PP/R2Q1RK1 w kq - 0 1", 5, 15833292))
                    .send();

            new TextResponse("Position 5 success: " +
                    testPosition("rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8  ", 5, 89941194))
                    .send();

            new TextResponse("Position 6 success: " +
                    testPosition("r4rk1/1pp1qppp/p1np1n2/2b1p1B1/2B1P1b1/P1NP1N2/1PP1QPPP/R4RK1 w - - 0 10 ", 5, 164075551))
                    .send();
        }
    }

    private boolean testPosition(String fen, int depth, long excpected) {
        new TextResponse("Testing fen: " + fen).send();
        long begin = System.nanoTime() / (long)1e6;

        Engine.Board b = new Engine.Board.Builder()
                .fen(fen)
                .build();
        Engine.SearchLimit limit = new Engine.SearchLimit();
        limit.capturesOnly = false;
        long rootMoves = b.perft(1, limit);
        int[] currentRootMove = { 1 };
        long perftResult = b.perft(depth, (move, count) -> {
            String counter = "(" + currentRootMove[0]++ + "/" + rootMoves + ")";
            String smove = Engine.Move.toString(move);
            System.out.println(smove + " " + counter + " - " + count);
        }, limit);

        long end = System.nanoTime() / (long)1e6;
        new TextResponse("Finished test in " + (end - begin) + "ms").send();

        return perftResult == excpected;
    }

}
