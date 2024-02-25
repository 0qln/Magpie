package Interface;

@FunctionalInterface
public interface ICommandBuilder
{
    ICommand buildForBoard(Engine.IBoard board);
}


