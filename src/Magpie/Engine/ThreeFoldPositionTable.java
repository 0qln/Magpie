package Engine;

public class ThreeFoldPositionTable {

    private final byte[] _occurances;
    private final int _capacity;
    

    private ThreeFoldPositionTable(int capacity) {
        _occurances = new byte[capacity];
        _capacity = capacity;
    }

    public static ThreeFoldPositionTable ofOrder(int order) {
        return new ThreeFoldPositionTable((int)Math.pow(2, order));
    }
    

    // When incrementing/decrementing, we should not need to check for overflows as 
    // the search will abort if the position occured more than thrice (three fold repitition).
    // In case that the search does not abort (ie. perft) we don't need to check 
    // for overflows, as the occurance of the position should not be relevant for such searches.
    
    public void decrement(long key) {
        _occurances[(int)Math.abs(key % _capacity)]--;
    }
    
    public void increment(long key) {
        _occurances[(int)Math.abs(key % _capacity)]++;
    }
    
    public byte get(long key) {
        return _occurances[(int)Math.abs(key % _capacity)];
    }
}
