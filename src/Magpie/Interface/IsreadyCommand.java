package Interface;

import Misc.Ptr;

public class IsreadyCommand extends Command
{
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
