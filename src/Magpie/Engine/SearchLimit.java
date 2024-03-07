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
    nodes = -1, 
    // timing 
    wtime = -1, btime = -1, winc = 0, binc = 0, movetime = -1;
    // game tree
    public MoveList searchmoves = null;
}
