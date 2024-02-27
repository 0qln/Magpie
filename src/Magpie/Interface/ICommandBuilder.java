package Interface;

import Misc.Ptr;

@FunctionalInterface
public interface ICommandBuilder
{
    ICommand buildForBoard(Ptr<Engine.IBoard> board);
}


