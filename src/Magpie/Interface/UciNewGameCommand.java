package Interface;

public class UciNewGameCommand extends Command {

    public UciNewGameCommand() {
        _forceSync = true;
    }

    static {
        Signature.register("ucinewgame", UciNewGameCommand.class, new Builder<>(() -> new UciNewGameCommand()));
    }

    @Override
    public boolean parseArgs(String[] args) {
        return true;
    }

    @Override
    public void run() {
        _state.board.set(new Engine.Board.Builder().build());
        _state.search.set(null);
    }

}
