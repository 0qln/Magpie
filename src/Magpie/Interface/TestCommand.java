package Interface;

import static Engine.Utils.countBits;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Random;
import java.util.logging.Logger;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;

import Engine.AlphaBetaSearchTree;
import Engine.Bishop;
import Engine.EpdInfo;
import Engine.IMoveDecoder;
import Engine.MoveFormat;
import Engine.Rook;
import Engine.SearchLimit;
import Engine.SlidingPiece;
import Engine.Zobrist;
import Misc.LoggerConfigurator;
import Misc.ProgramState;

public class TestCommand extends Command {
           
    private static final Logger _logger = LoggerConfigurator.configureLogger(TestCommand.class);
    
    private ProgramState _testState = new ProgramState();
    private int _fails = 0;
    private boolean _stopRequested = false;
    
    private boolean incrFails(boolean value) {
        if (!value) _fails ++;
        return value;
    }

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

        _state.runningTests.add(this);

        boolean all = params_getB("all");

        if (all || params_getB("fen-encode")) {
            // fen-decoding and LAN_UCI-decoding can be trusted.
             
            _logger.info("Begin test: fen-encode");
            
            new TextResponse(
                "FEN 1 success: " +
                incrFails(testFenEncoding(
                    "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", 
                    new String[] {}, 
                    MoveFormat.RawDec, 
                    "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1")))
                .send();
            
            new TextResponse(
                "FEN 2 success: " +
                incrFails(testFenEncoding(
                    "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", 
                    new String[] { "e2e4" },
                    MoveFormat.LongAlgebraicNotation_UCI, 
                    "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq - 0 1")))
                .send();
            
            new TextResponse(
                "FEN 3 success: " +
                incrFails(testFenEncoding(
                    "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", 
                    new String[] { "e2e4", "c7c5", "e4e5", "d7d5" },
                    MoveFormat.LongAlgebraicNotation_UCI, 
                    "rnbqkbnr/pp2pppp/8/2ppP3/8/8/PPPP1PPP/RNBQKBNR w KQkq d6 0 3")))
                .send();
            
            new TextResponse(
                "FEN 4 success: " +
                incrFails(testFenEncoding(
                    "r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1", 
                    new String[] { "e1c1" },
                    MoveFormat.LongAlgebraicNotation_UCI, 
                    "r3k2r/8/8/8/8/8/8/2KR3R b kq - 1 1")))
                .send();
            
            new TextResponse(
                "FEN 5 success: " +
                incrFails(testFenEncoding(
                    "r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1", 
                    new String[] { "e1g1" },
                    MoveFormat.LongAlgebraicNotation_UCI, 
                    "r3k2r/8/8/8/8/8/8/R4RK1 b kq - 1 1")))
                .send();
            
            new TextResponse(
                "FEN 6 success: " +
                incrFails(testFenEncoding(
                    "r3k2r/8/8/8/8/8/8/R4RK1 b kq - 1 1", 
                    new String[] { "e8c8" },
                    MoveFormat.LongAlgebraicNotation_UCI, 
                    "2kr3r/8/8/8/8/8/8/R4RK1 w - - 2 2")))
                .send();

        }
        
        if (all || params_getB("SAN-decode")) {
             
            _logger.info("Begin test: SAN-decode");

            // test normal move
            new TextResponse(
                "SAN 1 success: " +
                incrFails(testMove(
                    "e4", 
                    MoveFormat.StandardAlgebraicNotation, 
                    "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", 
                    "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq - 0 1")))
                .send();

            // test en passant
            new TextResponse(
                "SAN 2 success: " +
                incrFails(testMoves(
                    new String[] { "e4", "c5", "e5", "d5", "exd6" }, 
                    MoveFormat.StandardAlgebraicNotation, 
                    "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", 
                    "rnbqkbnr/pp2pppp/3P4/2p5/8/8/PPPP1PPP/RNBQKBNR b KQkq - 0 3")))
                .send();

            // test file ambuguity
            new TextResponse(
                "SAN 3 success: " +
                incrFails(testMoves(
                    new String[] { "e4", "h6", "Nc3", "c6", "Nge2" }, 
                    MoveFormat.StandardAlgebraicNotation, 
                    "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", 
                    "rnbqkbnr/pp1pppp1/2p4p/8/4P3/2N5/PPPPNPPP/R1BQKB1R b KQkq - 1 3")))
                .send();

            // test rank ambuguity
            new TextResponse(
                "SAN 4 success: " +
                incrFails(testMoves(
                    new String[] { "b3", "b6", "Bb2", "Bb7", "Nc3", "e6", "e3" ,"f6", "Nge2", "Nh6", "Nc1", "Nc6", "N3e2" }, 
                    MoveFormat.StandardAlgebraicNotation, 
                    "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", 
                    "r2qkb1r/pbpp2pp/1pn1pp1n/8/8/1P2P3/PBPPNPPP/R1NQKB1R b KQkq - 5 7")))
                .send();

            // test square ambuguity
            new TextResponse(
                "SAN 5 success: " +
                incrFails(testMoves(
                    new String[]  { "Be7", "Nc3e2" }, 
                    MoveFormat.StandardAlgebraicNotation, 
                    "r2qkb1r/pbpp2pp/1pn1pp1n/8/8/1PN1P1N1/PBPP1PPP/R1NQKB1R b KQkq - 5 7", 
                    "r2qk2r/pbppb1pp/1pn1pp1n/8/8/1P2P1N1/PBPPNPPP/R1NQKB1R b KQkq - 7 8")))
                .send();

            // test castling and captures
            new TextResponse(
                "SAN 6 success: " +
                incrFails(testMoves(
                    new String[] { "e4", "e5", "Nf3", "Nc6", "Bb5", "Qe7", "O-O", "d6", "Nxe5", "Be6", "Nf3", "O-O-O" }, 
                    MoveFormat.StandardAlgebraicNotation, 
                    "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", 
                    "2kr1bnr/ppp1qppp/2npb3/1B6/4P3/5N2/PPPP1PPP/RNBQ1RK1 w - - 3 7")))
                .send();

            // test pawn promotions
            new TextResponse(
                "SAN 7 success: " +
                incrFails(testMoves(
                    new String[] { "h8=Q", "bxa1=N" }, 
                    MoveFormat.StandardAlgebraicNotation, 
                    "r1k2rn1/ppp1qp1P/2npb3/1B6/8/5N2/PpPP1PPP/RNBQ1RK1 w - - 3 7", 
                    "r1k2rnQ/ppp1qp2/2npb3/1B6/8/5N2/P1PP1PPP/nNBQ1RK1 w - - 0 8")))
                .send();

        }
        
        if (params_getB(("magics"))) {

            _logger.info("Begin test: magics");
            
            var rng = new Random();

            if (params_getB("rook")) {
                
                findBestMagics("rook", rng, Rook.generator, Rook.generator);

            }

            if (params_getB("bishop")) {
                
                findBestMagics("bishop", rng, Bishop.generator, Bishop.generator);
                                
            }
            
        }
        
        if (params_getB("zobrist")) {
             
            _logger.info("Begin test: zobrist");
            
            // TODO: make a better way of testing seeds.

            // find a seed with minimal collisions
            var rng = new Random();
            var collisionsMin = Long.MAX_VALUE;
            long seed, bestSeed = 0;
            do {
                seed = Zobrist.getSeed();

                Engine.Board board = new Engine.Board.Builder()
                        .fen("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1")
                        .build();
                Engine.AlphaBetaSearchTree searchTree = new AlphaBetaSearchTree(board);

                _testState.board.set(board);
                _testState.search.set(searchTree);

                SearchLimit limit = new SearchLimit();
                // The JIT may optimize the search during runtime
                // This is why we search a fixed amount of nodes instead of a 
                // time limit.
                limit.nodes = 300000;
                searchTree.begin(limit);
                long collisions = searchTree.getTT().getCollisions();
                long nodes = searchTree.getNodesSearched();
                
                if (nodes != limit.nodes) {
                    _logger.warning("Incomplete Zobrist measurement (" + nodes + "/" + limit.nodes + " nodes). Abandoning Zobrist test.");
                    break;
                }
                
                if (collisions == 0) {
                    new TextResponse("Optimal seed: < " + seed  + " >  (" + collisions + " collisions)").send();
                    break;
                }

                new TextResponse("Testing seed: < " + seed  + " >  (" + collisions + " collisions)").send();

                if (collisions < collisionsMin) {
                    collisionsMin = collisions;
                    bestSeed = seed;
                    new TextResponse("New Best ^ ").send();
                }

                // Do this at the end such that the minimum is set to the currently 
                // hardcoded value.
                Zobrist.initSeed(rng.nextLong());
            }
            while (collisionsMin > 0 && _stopRequested == false);
            
            new TextResponse("Best seed found: " + bestSeed).send();
        }
        
        if (all || params_getB("eret")) {
             
            _logger.info("Begin test: eret");

            // Eigenmann Rapid Engine Test
            //https://www.chessprogramming.org/Eigenmann_Rapid_Engine_Test
            
            // TODO: execute benchmarked.
            try (Stream<String> stream = Files.lines(Paths.get("Tests/ERET.txt"))) {
                stream.forEach(epd -> {
                    new TextResponse("EPD: " + epd).send();
                    Engine.Board.Builder builder = new Engine.Board.Builder();
                    Engine.Board board = builder.epd(epd).build();
                    _testState.board.set(board);
                    EpdInfo info = builder.epdInfoResult;
                    new TextResponse("ID: " + info.id).send();
                    if (info.bm != null) new TextResponse("Best Moves: " + Misc.Utils.arrStr(info.bm)).send();
                    if (info.am != null) new TextResponse("Avoid Moves: " + Misc.Utils.arrStr(info.am)).send();
                    Engine.AlphaBetaSearchTree search = new AlphaBetaSearchTree(board);
                    Engine.IMoveDecoder SANdecoder = board.getMoveDecoder(MoveFormat.StandardAlgebraicNotation);
                    // TODO: does the ERET include best move(s) or only best move?
                    int bestMovesCnt = info.bm == null ? 0 : info.bm.length;
                    short[] bestMoves = new short[bestMovesCnt];
                    for (int i = 0; i < bestMovesCnt; i++) {
                        bestMoves[i] = SANdecoder.decode(info.bm[i]);
                    }
                    int avoidMovesCnt = info.am == null ? 0 : info.am.length;
                    short[] avoidMoves = new short[avoidMovesCnt];
                    for (int i = 0; i < avoidMovesCnt; i++) {
                        avoidMoves[i] = SANdecoder.decode(info.am[i]);
                    }
                    _testState.search.set(search);
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
                        
                        short pv = su.pvline[0];
                        boolean isBest = bestMovesCnt == 0 || ArrayUtils.contains(bestMoves, pv);
                        boolean isWorst = ArrayUtils.contains(avoidMoves, pv);
                        if (isBest && !isWorst) {
                            new TextResponse("EPD solved.").send();
                            search.stop();
                        }
                    });
                    Engine.SearchLimit limit = new SearchLimit();
                    search.begin(limit);
                });
            }
            catch (IOException e) {
                _logger.severe("Error: " + e.getMessage());
            }
            
        }

        if (all || params_getB("movegen")) {
             
            _logger.info("Begin test: movegen");
            
            long begin = System.currentTimeMillis();

            new TextResponse("Position 1 success: " +
                    incrFails(testPosition("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", 5, 4865609L)))
                    .send();

            new TextResponse("Position 2 success: " +
                    incrFails(testPosition("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1", 5, 193690690L)))
                    .send();

            new TextResponse("Position 3 success: " +
                    incrFails(testPosition("8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - - 0 1", 6, 11030083L)))
                    .send();

            new TextResponse("Position 4 success: " +
                    incrFails(testPosition("r3k2r/Pppp1ppp/1b3nbN/nP6/BBP1P3/q4N2/Pp1P2PP/R2Q1RK1 w kq - 0 1", 5, 15833292)))
                    .send();

            new TextResponse("Position 5 success: " +
                    incrFails(testPosition("rnbq1k1r/pp1Pbppp/2p5/8/2B5/8/PPP1NnPP/RNBQK2R w KQ - 1 8  ", 5, 89941194)))
                    .send();

            new TextResponse("Position 6 success: " +
                    incrFails(testPosition("r4rk1/1pp1qppp/p1np1n2/2b1p1B1/2B1P1b1/P1NP1N2/1PP1QPPP/R4RK1 w - - 0 10 ", 5, 164075551)))
                    .send();
            
            new TextResponse("Completed Movegen testsuite in " + (System.currentTimeMillis() - begin) + " ms").send();
        }
        
        new TextResponse("\nTotal fails: " + _fails).send();
        
        _state.runningTests.remove(this);

    }
    
    // A magic is expensive if the maxIndex is high wrt. the bits available.
    public record MagicInfo(long magic, int bits, int maxIndex, int square, int free, double cost) { 
        public MagicInfo(long magic, int bits, int maxIndex, int square, int free) {
            this(magic, bits, maxIndex, square, free, (double)maxIndex / (double)(1 << bits));
        }
    }
    
    int sizeOfMagics(MagicInfo[] infos) {
        int size = Stream.of(infos).mapToInt(info -> info.maxIndex * Long.BYTES).sum();
        return size;
    }
    
    void findBestMagics(String pt, Random rng, SlidingPiece.MoveGenerator gen, SlidingPiece.IMoveLookup lookup) {
        TextResponse.send("Searching for "+pt+" magics");
        
        var infos = new MagicInfo[64];
        
        for (int square = 0; square < 64; square++) {

            infos[square] =  findMagic(
                rng,
                gen,
                square, 
                lookup.getMagicBits()[square],
                Integer.MAX_VALUE,
                Integer.MAX_VALUE);
            
            TextResponse.send("[SQ"+square+"] Found magic: < " + infos[square].toString() + " >");
        }

        TextResponse.send("Size: " + (sizeOfMagics(infos) / 1024)  + "kiB");
        
        for (int square = 0; !_stopRequested; square = ++square % 64) {
            var newInfo = findMagic(
                rng,
                gen,
                square, 
                lookup.getMagicBits()[square],
                infos[square].maxIndex,
                100000);
            
            if (newInfo == null)
                continue;
            
            infos[square] = newInfo;
            TextResponse.send("[SQ"+square+"] Found magic: < " + infos[square].toString() + " >");
            
            TextResponse.send("Size: " + (sizeOfMagics(infos) / 1024)  + "kiB");
        }
       
        SlidingPiece.MoveGenerator.Initialize(gen, lookup);
        
        StringBuilder sb = new StringBuilder();
        sb.append("{ ");
        for (int square = 0; square < 64; square++)
            sb.append(infos[square].magic).append("L, ");
        sb.append("} ");

        TextResponse.send(sb.toString());       
    }

    MagicInfo findMagic(Random rng, SlidingPiece.MoveGenerator gen, int square, int magicShift, int keyMin, int attemptsMax) {
        int bufferSize = (1 << 6) * (1 << 6);
        long[] blockers = new long[bufferSize], attacks = new long[bufferSize], buffer = new long[bufferSize];
        long maxBlockers, magic;
        int numBlockerCompositions;

        maxBlockers = gen.relevantOccupancy(square);
        numBlockerCompositions = 1 << countBits(maxBlockers);

        Arrays.setAll(blockers, i -> gen.mapBits(i, maxBlockers));
        Arrays.setAll(attacks, i -> gen.computeAttacks(square, blockers[i]));

        for (int attempts = 0; attempts < attemptsMax; attempts++) {

            magic = rng.nextLong() & rng.nextLong() & rng.nextLong();
            int keyMax = 0;

            Arrays.fill(buffer, 0);

            var fail = false;
            for (int i = 0; !fail && i < numBlockerCompositions; i++) {
                int key = gen.getKey(blockers[i], magic, magicShift);
                keyMax = Math.max(keyMax, key);

                if (buffer[key] == 0L)
                    buffer[key] = attacks[i];
                else if (buffer[key] != attacks[i])
                    fail = true;
            }
            if (!fail && keyMax < keyMin) {
                int fre = 0;
                for (int i = 0; i <= keyMax; i++) {
                    if (buffer[i] == 0L)
                        fre++;
                }
                return new MagicInfo(magic, magicShift, keyMax, square, fre);
            }
        }
        return null;
    }

    private boolean testPosition(String fen, int depth, long excpected) {
        new TextResponse("Testing fen: " + fen).send();
        long begin = System.nanoTime() / (long)1e6;

        Engine.Board b = new Engine.Board.Builder()
                .fen(fen)
                .build();
        _testState.board.set(b);
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
            new TextResponse("Input:    " + fenOrigin).send();
            new TextResponse("Result:   " + b.fen()).send();
            new TextResponse("Expected: " + expectedFEN).send();
        }

        return result;
    }

    public void stop() {
        _stopRequested = true;
        if (!_testState.search.isNull())
            _testState.search.get().stop();
        _logger.info("Stop requested.");
    }

}
