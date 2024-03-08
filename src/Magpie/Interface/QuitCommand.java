package Interface;


public class QuitCommand extends Command
{
    public QuitCommand() {
        _forceSync = true;
    }

    static {
        Signature.register("quit", QuitCommand.class, new Builder<>(() -> new QuitCommand()));
    }
    
    @Override
    public boolean parseArgs(String[] args) {
        return true;
    }

    public void run() {
        Interface.Main.quit();
    }
}
