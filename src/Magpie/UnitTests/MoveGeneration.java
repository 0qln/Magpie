package UnitTests;

public class MoveGeneration {

    public void position1() {
        testPosition("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", 5, 4865609L);
    }

    public void position2() {
        testPosition("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1", 5, 193690690L);
    }

    public void position3() {
        testPosition("8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - - 0 1", 6, 11030083L);
    }

    private void testPosition(String fen, int depth, long excpected) {
        Engine.Board b = new Engine.Board.Builder()
                .fen(fen)
                .build();

        Engine.SearchLimit limit = new Engine.SearchLimit();
        limit.capturesOnly = false;
        long perftResult = b.perft(5, limit);

        assert(excpected == perftResult);
    }
}
