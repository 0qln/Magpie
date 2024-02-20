public interface ICommandBuilder<TCommand>
{
    ICommand buildForBoard(IBoard board);
}
