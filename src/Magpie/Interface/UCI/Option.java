package Interface.UCI;

import java.util.Optional;
import Interface.*;

public class Option<T> {
    private final String _name;
    private final OptionType _type;
    private final Optional<T> _min, _max;
    private final Optional<T[]> _vars;
    private final T _default;
    private T _value;

    public Option(String name, OptionType type, T defaultVal, Optional<T> min, Optional<T> max, Optional<T[]> vars) {
        _name = name;
        _type = type;
        _min = min;
        _max = max;
        _default = defaultVal;
        _value = defaultVal;
        _vars = vars;
    }

    public String getName() {
        return _name;
    }

    public OptionType getType() {
        return _type;
    }

    public Optional<T> getDefault() {
        return Optional.ofNullable(_default);
    }

    public Optional<T> getMin() {
        return _min;
    }

    public Optional<T> getMax() {
        return _max;
    }

    public Optional<T[]> getVars() {
        return _vars;
    }

    public void setValue(T value) {
        _value = value;
    }
    
    public T getValue() {
        return _value;
    }
}
