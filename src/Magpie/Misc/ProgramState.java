package Misc;

public class ProgramState
{
    public final Ptr<Engine.IBoard> board = new Ptr<>(null);
    public final Ptr<Engine.ISearchTree> search = new Ptr<>(null);
}
