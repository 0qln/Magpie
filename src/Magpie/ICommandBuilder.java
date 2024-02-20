public interface ICommandBuilder<TCommand>
{
    ICommand BuildForBoard(IBoard board);
}
