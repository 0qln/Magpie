package Interface;

public class PrintCommand extends Command {
    static {
        Signature.register("print", PrintCommand.class, new Builder<>(() -> new PrintCommand()));
    }

    @Override
    public boolean parseArgs(String[] args) {
        return true;
    }

    public void run() {
        new TextResponse(_board.get().toString()).send();
    }
}
