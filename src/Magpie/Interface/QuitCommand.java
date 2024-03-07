package Interface;


public class QuitCommand extends Command
{
    static {
        Signature.register("quit", QuitCommand.class, new Builder<>(() -> new QuitCommand()));
    }
    
    @Override
    public boolean parseArgs(String[] args) {
        return true;
    }

    public void run() {

        System.exit(0);
    }
}
