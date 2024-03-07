package Engine;

public class SearchUpdate {

    public final int eval;
    public final int depth;
    public final int seldepth;
    public final long nodes, nps;
    public final short[] pvline;
    // ...
    
    public SearchUpdate(int eval, int depth, int seldepth, long nodes, short[] pvline, long nps) {
        this.eval = eval;
        this.depth = depth;
        this.seldepth = seldepth;
        this.nodes = nodes;
        this.pvline = pvline;
        this.nps = nps;
    }
}
