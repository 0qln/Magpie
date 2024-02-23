package Interface;

public interface ICommandBuilder<TCommand>
{
    ICommand buildForBoard(Engine.IBoard board);
}
