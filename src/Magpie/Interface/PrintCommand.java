package Interface;

public class PrintCommand extends Command {
    static {
        Signature.register("print", PrintCommand.class, new Builder<>(() -> new PrintCommand()));
        Signature.register("d"    , PrintCommand.class, new Builder<>(() -> new PrintCommand()));
    }

    @Override
    public boolean parseArgs(String[] args) {
        return true;
    }

    public void run() {
        TextResponse.send("");
        TextResponse.send(_state.board.get().toString());
        TextResponse.send("");
        TextResponse.send("Fen: " + _state.board.<Engine.Board>getAs().fen());
        TextResponse.send("Key: " + _state.board.<Engine.Board>getAs().getKey());
        TextResponse.send("");
    }
}
