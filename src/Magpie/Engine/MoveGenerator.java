package Engine;

public abstract class MoveGenerator
{
    // Returns index of next empty list slot.
    abstract int generate(short[] list, int index, Board board, int color);

    // public long ray(int from, int direction) {
    //     return -1;
    // }

    // public long attackers(int targetSquare) {

    // }
}
