package Interface;

import java.util.Random;

import Engine.Board;
import Engine.IMoveDecoder;
import Engine.MoveFormat;
import Engine.Zobrist;

public class TestCommand extends Command {

    static {
        Signature.register("test", TestCommand.class, new Builder<>(() -> new TestCommand()));
    }
    
    public TestCommand() {
        // _forceSync = true;
    }

    @Override
    public boolean parseArgs(String[] args) {
        for (int i = 0; i < args.length; i++)
            params_put(args[i], true);
        return true;
    }

    @Override
    public void run() {
        boolean all = params_getB("all");
        if (all || params_getB("fen-encode")) {
            // fen-decoding and LAN_UCI-decoding can be trusted.
            
            new TextResponse(
                "FEN 1 success: " +
                testFenEncoding(
                    "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", 
                    new String[] {}, 
                    MoveFormat.RawDec, 
                    "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"))
                .send();
            
            new TextResponse(
                "FEN 2 success: " +
                testFenEncoding(
                    "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", 
                    new String[] { "e2e4" },
                    MoveFormat.LongAlgebraicNotation_UCI, 
                    "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq - 0 1"))
                .send();
            
            new TextResponse(
                "FEN 3 success: " +
                testFenEncoding(
                    "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", 
                    new String[] { "e2e4", "c7c5", "e4e5", "d7d5" },
                    MoveFormat.LongAlgebraicNotation_UCI, 
                    "rnbqkbnr/pp2pppp/8/2ppP3/8/8/PPPP1PPP/RNBQKBNR w KQkq d6 0 3"))
                .send();
            
            new TextResponse(
                "FEN 4 success: " +
                testFenEncoding(
                    "r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1", 
                    new String[] { "e1c1" },
                    MoveFormat.LongAlgebraicNotation_UCI, 
                    "r3k2r/8/8/8/8/8/8/2KR3R b kq - 1 1"))
                .send();
            
            new TextResponse(
                "FEN 5 success: " +
                testFenEncoding(
                    "r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1", 
                    new String[] { "e1g1" },
                    MoveFormat.LongAlgebraicNotation_UCI, 
                    "r3k2r/8/8/8/8/8/8/R4RK1 b kq - 1 1"))
                .send();
            
            new TextResponse(
                "FEN 6 success: " +
                testFenEncoding(
                    "r3k2r/8/8/8/8/8/8/R4RK1 b kq - 1 1", 
                    new String[] { "e8c8" },
                    MoveFormat.LongAlgebraicNotation_UCI, 
                    "2kr3r/8/8/8/8/8/8/R4RK1 w - - 2 2"))
                .send();

        }
        if (all || params_getB("SAN-decode")) {

            // test normal move
            new TextResponse(
                "SAN 1 success: " +
                testMove(
                    "e4", 
                    MoveFormat.StandardAlgebraicNotation, 
                    "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", 
                    "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq - 0 1"))
                .send();

            // test en passant
            new TextResponse(
                "SAN 2 success: " +
                testMoves(
                    new String[] { "e4", "c5", "e5", "d5", "exd6" }, 
                    MoveFormat.StandardAlgebraicNotation, 
                    "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", 
                    "rnbqkbnr/pp2pppp/3P4/2p5/8/8/PPPP1PPP/RNBQKBNR b KQkq - 0 3"))
                .send();

            // test file ambuguity
            new TextResponse(
                "SAN 3 success: " +
                testMoves(
                    new String[] { "e4", "h6", "Nc3", "c6", "Nge2" }, 
                    MoveFormat.StandardAlgebraicNotation, 
                    "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", 
                    "rnbqkbnr/pp1pppp1/2p4p/8/4P3/2N5/PPPPNPPP/R1BQKB1R b KQkq - 1 3"))
                .send();

            // test rank ambuguity
            new TextResponse(
                "SAN 4 success: " +
                testMoves(
                    new String[] { "b3", "b6", "Bb2", "Bb7", "Nc3", "e6", "e3" ,"f6", "Nge2", "Nh6", "Nc1", "Nc6", "N3e2" }, 
                    MoveFormat.StandardAlgebraicNotation, 
                    "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", 
                    "r2qkb1r/pbpp2pp/1pn1pp1n/8/8/1P2P3/PBPPNPPP/R1NQKB1R b KQkq - 5 7"))
                .send();

            // test square ambuguity
            new TextResponse(
                "SAN 5 success: " +
                testMoves(
                    new String[]  { "Be7", "Nc3e2" }, 
                    MoveFormat.StandardAlgebraicNotation, 
                    "r2qkb1r/pbpp2pp/1pn1pp1n/8/8/1PN1P1N1/PBPP1PPP/R1NQKB1R b KQkq - 5 7", 
                    "r2qk2r/pbppb1pp/1pn1pp1n/8/8/1P2P1N1/PBPPNPPP/R1NQKB1R b KQkq - 7 8"))
                .send();

            // test castling and captures
            new TextResponse(
                "SAN 6 success: " +
                testMoves(
                    new String[] { "e4", "e5", "Nf3", "Nc6", "Bb5", "Qe7", "O-O", "d6", "Nxe5", "Be6", "Nf3", "O-O-O" }, 
                    MoveFormat.StandardAlgebraicNotation, 
                    "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", 
                    "2kr1bnr/ppp1qppp/2npb3/1B6/4P3/5N2/PPPP1PPP/RNBQ1RK1 w - - 3 7"))
                .send();

            // test pawn promotions
            new TextResponse(
                "SAN 7 success: " +
                testMoves(
                    new String[] { "h8=Q", "bxa1=N" }, 
                    MoveFormat.StandardAlgebraicNotation, 
                    "r1k2rn1/ppp1qp1P/2npb3/1B6/8/5N2/PpPP1PPP/RNBQ1RK1 w - - 3 7", 
                    "r1k2rnQ/ppp1qp2/2npb3/1B6/8/5N2/P1PP1PPP/nNBQ1RK1 w - - 0 8"))
                .send();

        }
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
        if (all || params_getB("eret")) {
            //https://www.chessprogramming.org/Eigenmann_Rapid_Engine_Test
        }
        if (all || params_getB("movegen")) {
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

    
    private boolean testMove(String move, MoveFormat format, String fenOrigin, String fenResult) {
        Engine.Board b = new Engine.Board.Builder()
                .fen(fenOrigin)
                .build();
        
        IMoveDecoder decoder = b.getMoveDecoder(format);
        
        b.makeMove(decoder.decode(move));
        
        String fen = b.fen();
        boolean result = fen.equals(fenResult);
        
        if (!result) {
            new TextResponse("\nFAIL").send();
            new TextResponse("Move:     " + move).send();
            new TextResponse("FEN:      " + fenOrigin).send();
            new TextResponse("Result:   " + fen).send();
            new TextResponse("Expected: " + fenResult).send();
        }
        
        return result;
    }
    
    private boolean testMoves(String[] moves, MoveFormat format, String fenOrigin, String fenResult) {
        Engine.Board b = new Engine.Board.Builder()
                .fen(fenOrigin)
                .build();
        
        IMoveDecoder decoder = b.getMoveDecoder(format);
        
        for (String move : moves) {
            b.makeMove(decoder.decode(move));
        }
        
        String fen = b.fen();
        boolean result = fen.equals(fenResult);
        
        if (!result) {
            new TextResponse("\nFAIL").send();
            new TextResponse("Moves:    " + String.join(" ", moves)).send();
            new TextResponse("FEN:      " + fenOrigin).send();
            new TextResponse("Result:   " + fen).send();
            new TextResponse("Expected: " + fenResult).send();
        }
        
        return result;
    }
    
    private boolean testFenEncoding(String fenOrigin, String[] moves, MoveFormat format, String expectedFEN) {
        Engine.Board b = new Engine.Board.Builder()
                .fen(fenOrigin)
                .build();
        
        IMoveDecoder decoder = b.getMoveDecoder(format);
        
        for (String move : moves) {
            b.makeMove(decoder.decode(move));
        }
        
        boolean result = b.fen().equals(expectedFEN);
        
        if (!result) {
            new TextResponse("\nFAIL").send();
            new TextResponse(b.toString()).send();
            new TextResponse("Result:   " + b.fen()).send();
            new TextResponse("Expected: " + expectedFEN).send();
        }

        return result;
    }
}
