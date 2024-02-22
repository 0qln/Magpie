public abstract class UciPositionCommand<TMove> extends UciCommand
{
    private String[] _moves;
    
    public UciPositionCommand(IBoard<TMove> board, String[] moves) {
        super(board);
        _moves = moves;
    }
    
}
