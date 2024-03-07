package Interface;

import java.util.Hashtable;

import Misc.Ptr;

// https://github.com/official-stockfish/Stockfish/wiki/UCI-&-Commands#standard-commands

public abstract class Command extends ICommand {

    // Java does not load the static init if the class is not explicitly mentioned.
    // >:(

    public static class Signature<TCommand extends Command> {
        public final String protocolName;
        public final Class<TCommand> commandType;
        public final Builder<TCommand> commandBuilder;

        private Signature(String name, Class<TCommand> type, Builder<TCommand> builder) {
            this.protocolName = name;
            this.commandType = type;
            this.commandBuilder = builder;
        }

        public static <T extends Command> Signature<?> register(String name, Class<T> type, Builder<T> builder) {
            Signature<T> ret = new Signature<T>(name, type, builder);
            _commands.put(name, ret);
            return ret;
        }

        public static Signature<?> get(String name) {
            if (_commands.containsKey(name)) {
                return _commands.get(name);
            } else {
                return null;
            }
        }

        @Override
        public int hashCode() {
            return protocolName.hashCode();
        }

        public static final Signature<?>[] enumerate() {
            Signature<?>[] result = new Signature[_commands.size()];
            _commands.values().toArray(result);
            return result;
        }

    }

    private static final Hashtable<String, Signature<?>> _commands = new Hashtable<String, Signature<?>>();

    protected Ptr<Engine.IBoard> _board;
    protected String[] _args;
    protected boolean _canRun;

    protected Hashtable<String, Object> _params = new Hashtable<String, Object>();

    protected void params_put(String key, Object value) {
        _params.put(key, value);
    }

    @SuppressWarnings("unchecked")
    protected <T> T params_get(String key) {
        if (_params.containsKey(key))
            return (T) _params.get(key);
        else
            return null;
    }

    protected boolean params_getB(String key) {
        if (_params.containsKey(key))
            if (_params.get(key) instanceof Boolean)
                return (boolean) _params.get(key);
            else
                return _params.get(key) != null;
        return false;
    }

    public boolean canRun() {
        return _canRun;
    }

    // // Technically this is unneccesary, but it forces each command to register
    // // itself, or else it is impossible to create such command. (Debugging)
    // protected Command(Signature<?> callerCommand) {
    // if (!_commands.contains(callerCommand)) {
    // throw new ExceptionInInitializerError("An unregistered UCI command was
    // created.");
    // }
    // }

    public abstract boolean parseArgs(String[] args);

    public final static class Builder<TCommand extends Command> extends Misc.Builder<TCommand> {

        @Required
        protected String[] _args;
        @Required
        protected Ptr<Engine.IBoard> _board;

        private final IInstanceGenerator<TCommand> _generator;

        @FunctionalInterface
        public static interface IInstanceGenerator<TCommand extends Command> {
            public TCommand fetch();
        }

        public Builder(IInstanceGenerator<TCommand> generator) {
            _generator = generator;
        }

        public Builder<TCommand> args(String[] args) {
            _args = args;
            return this;
        }

        public Builder<TCommand> board(Ptr<Engine.IBoard> board) {
            _board = board;
            return this;
        }

        @Override
        protected TCommand _buildT() {
            TCommand instance = _generator.fetch();
            instance._args = _args;
            instance._board = _board;
            instance._canRun = instance.parseArgs(_args);
            _args = null;
            _board = null;
            return instance;
        }

    }

    public static class Parameter {
        public String name;
        public Object[] values;

        public Parameter(String name, Object value) {
            this.name = name;
            this.values = new Object[] { value };
        }

        public Parameter(String name, Object[] values) {
            this.values = values;
            this.name = name;
        }

        public <T> T as() {
            return (T) values[0];
        }

        public <T> T as(int index) {
            return (T) values[index];
        }
    }

}
