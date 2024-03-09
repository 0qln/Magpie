package Interface;

public abstract class ICommand implements Runnable
{
    public abstract boolean canRun();
    public abstract void runAsync();
    public abstract void runSync();
    public abstract boolean shouldSync();
}
