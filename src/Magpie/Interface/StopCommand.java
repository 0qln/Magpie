package Interface;

public class StopCommand extends Command
{
    static {
        Signature.register("stop", StopCommand.class, new Builder<>(() -> new StopCommand()));
    }
    
    @Override
    public boolean parseArgs(String[] tokens) {
        return true;
    }
    
    @Override
    public void run() {
        if (!_state.search.isNull()) {
            _state.search.get().stop();
            _state.search.set(null);
        }
        
        for (TestCommand test : _state.runningTests) {
            test.stop();
        }
    }
}
