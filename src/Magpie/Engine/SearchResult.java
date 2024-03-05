package Engine;

public class SearchResult {

    public final int eval;
    public final int depth;
    public final int seldepth;
    // ...
    
    public SearchResult(int eval, int depth, int seldepth) {
        this.eval = eval;
        this.depth = depth;
        this.seldepth = seldepth;
    }
}
