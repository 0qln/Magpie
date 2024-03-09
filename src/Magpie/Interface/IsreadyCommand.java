package Interface;

public class IsreadyCommand extends Command
{
    public IsreadyCommand() {
        _forceSync = true;
    }

    static {
        Signature.register("isready", IsreadyCommand.class, new Builder<>(() -> new IsreadyCommand()));
    }

    @Override
    public boolean parseArgs(String[] args) {
        return true;
    }

    public void run() {
        new ReadyokResponse().send();
    }
}
