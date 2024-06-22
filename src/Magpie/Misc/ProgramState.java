package Misc;

import java.util.ArrayList;

public class ProgramState
{
    public final Ptr<Engine.IBoard> board = new Ptr<>(null);
    public final Ptr<Engine.ISearchTree> search = new Ptr<>(null);
    public final ArrayList<Interface.TestCommand> runningTests = new ArrayList<>();
}
