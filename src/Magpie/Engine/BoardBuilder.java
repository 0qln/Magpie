package Engine;

public abstract class BoardBuilder<TBoard extends IBoard> extends Misc.Builder<TBoard> {
    public abstract BoardBuilder<TBoard> tokens(String[] tokens, TokenFormat format);
}
