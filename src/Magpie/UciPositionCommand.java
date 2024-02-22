public abstract class UciPositionCommand extends UciCommand
{
    private String[] _moves;
    
    public UciPositionCommand(IBoard board, String[] moves) {
        super(board);
        _moves = moves;
    }
    
    @SuppressWarnings("unchecked")
    public void run() {
        runExtended();
        for (String move : _moves) {
            _board.makeMove(_board.getMoveDecoder().decode(move));
        }
    }

    protected abstract void runExtended();
}
