package Interface;


@FunctionalInterface
public interface ICommandBuilder
{
    ICommand buildForBoard(Misc.ProgramState state);
}


