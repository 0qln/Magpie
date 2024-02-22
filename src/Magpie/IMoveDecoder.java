@FunctionalInterface
public interface IMoveDecoder<TMove>
{
    TMove decode(String longAlgebraicNotation);
}
