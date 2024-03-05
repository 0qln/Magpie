package Interface;

public abstract class ICommand implements Runnable
{
    public abstract boolean canRun();
}
