import java.util.Optional;

public class Opponent
{
    public Opponent(String name, Title title, Optional<Integer> elo, EntityType type)
    {
    }

    public static final Opponent getDefault() { 
        return new Opponent(null, null, Optional.empty(), null); 
    }
}
