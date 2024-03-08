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
        if (_state.search.isNull()) {
            return;
        }
        _state.search.get().stop();
        _state.search.set(null);
        Interface.Main.quit();
    }
}
