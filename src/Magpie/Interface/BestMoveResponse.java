package Interface;

public class BestMoveResponse extends Response {

    private final String _move, _ponder;

    public BestMoveResponse(String move, String ponder) {
        _move = move;
        _ponder = ponder;
    }

    @Override
    protected void executeSend() {
        StringBuilder result = new StringBuilder();
        result.append("bestmove ").append(_move).append(' ');
        if (_ponder != null) result.append(" ponder ").append(_ponder);
        System.out.println(result.toString());
    }

}
