package Engine;

public class SearchLimit {
    public static final int DEPTH = 200;

    public int 
    // timing
    movestogo, 
    // game tree
    depth = DEPTH, mate;

    public long 
    // game tree
    nodes, 
    // timing 
    wtime, btime, winc, binc, movetime;
    // game tree
    public MoveList searchmoves = null;
}
