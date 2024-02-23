package UCI;

import java.util.*;

public final class Config
{
    public static boolean Debug = false;
    
    public static final UciOption<?>[] Options = {
        new UciOption<>("UCI_Opponent", UCI.OptionType.String, Opponent.getDefault(), Optional.empty(), Optional.empty(), Optional.empty())
    };
 
    @SuppressWarnings("unchecked")
    public static <T> Optional<UciOption<T>> getOption(String name) {
        return Arrays.stream(Options)
            .filter(opt -> name.equals(opt.getName()))
            .map(opt -> (UciOption<T>)opt)
            .findFirst();
    }
}
