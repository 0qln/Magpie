package Interface;

public abstract class ICommand implements Runnable
{
    public abstract boolean canRun();
    public abstract void runAsync();
    public abstract boolean shouldSync();
}
