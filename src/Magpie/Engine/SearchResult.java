package Engine;

public class SearchResult {

    public final short bestMove, ponderMove;

    public SearchResult(short bestMove, short ponderMove) {
        this.bestMove = bestMove;
        this.ponderMove = ponderMove;
    } 

}
