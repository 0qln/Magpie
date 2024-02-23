package Interface;

import java.util.Arrays;
import java.util.Optional;

public final class Config {
    public static boolean Debug = false;
    
    public static final Interface.UCI.Option<?>[] UCI_Options = {
        new Interface.UCI.Option<>("UCI_Opponent", OptionType.String, Opponent.getDefault(), Optional.empty(), Optional.empty(), Optional.empty())
    };
 
    @SuppressWarnings("unchecked")
    public static <T> Optional<Interface.UCI.Option<T>> getOption(String name) {
        return Arrays.stream(UCI_Options)
            .filter(opt -> name.equals(opt.getName()))
            .map(opt -> (Interface.UCI.Option<T>) opt)
            .findFirst();
    }
}

