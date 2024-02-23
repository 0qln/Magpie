package UCI;

import java.util.Optional;

public class Opponent
{
    private String _name;
    private Title _title;
    private Optional<Integer> _elo;
    private EntityType _type;
    
    public Opponent(String name, Title title, Optional<Integer> elo, EntityType type)
    {
        _name = name;
        _title = title;
        _elo = elo;
        _type = type;
    }

    public static final Opponent getDefault() { 
        return new Opponent(null, null, Optional.empty(), EntityType.Unknown); 
    }
    
    public String toString() {
        String result = _type + " " + _title + " " + _name;
        if (_elo.isPresent()) 
            result +=  " (" + _elo + ")";
        return "{ " + result + " }";
    }
}
