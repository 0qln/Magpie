import java.util.*;

public class Config
{
    public static boolean Debug = false;
    
    public static final UciOption<?>[] Options = {
    };
 
    @SuppressWarnings("unchecked")
    public static <T> Optional<UciOption<T>> getOption(String name) {
        return Arrays.stream(Options)
            .filter(opt -> name.equals(opt.getName()))
            .map(opt -> (UciOption<T>)opt)
            .findFirst();
    }
}
