package Engine;

public class SearchUpdate {

    public int eval;
    public int depth;
    public int seldepth;
    public long nodes, nps;
    public short[] pvline;
    public int currmovenumber;
    public short currmove;

    public SearchUpdate(int eval, int depth, int seldepth, long nodes, short[] pvline, long nps) {
        this.eval = eval;
        this.depth = depth;
        this.seldepth = seldepth;
        this.nodes = nodes;
        this.pvline = pvline;
        this.nps = nps;
    }

    public SearchUpdate(int depth, short currmove, int currmovenumber) {
        this.depth = depth;
        this.currmove = currmove;
        this.currmovenumber = currmovenumber;
    }
}
