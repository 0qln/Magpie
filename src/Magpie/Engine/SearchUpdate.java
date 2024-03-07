package Engine;

public class SearchUpdate {

    public final int eval;
    public final int depth;
    public final int seldepth;
    public final long nodes;
    public final short[] pvline;
    // ...
    
    public SearchUpdate(int eval, int depth, int seldepth, long nodes, short[] pvline) {
        this.eval = eval;
        this.depth = depth;
        this.seldepth = seldepth;
        this.nodes = nodes;
        this.pvline = pvline;
    }
}
