package Interface;

import java.util.Optional;

@FunctionalInterface
public interface IOptionValueParser<T> {
    public Optional<T> parse(String[] rawData);
}
