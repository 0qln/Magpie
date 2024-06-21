package Engine;

public class MoveDecoder_Raw implements IMoveDecoder {
    @Override
    public short decode(String move) {
        return Short.parseShort(move.substring(1, move.length()));
    }
}