package Engine;

import Engine.AlphaBetaSearchTree.Line;

public class TranspositionTable {
    private final Entry[] _entries;
    private final int _capacity;
    private int _collisions = 0;
    private int _entryCount = 0;
    
    public int getCollisions() {
        return _collisions;
    }
    
    public int getEntryCount() {
        return _entryCount;
    }
    
    public int getCapacity() {
        return _capacity;
    }

    private TranspositionTable(int elements) {
        _capacity = elements;
        _entries = new Entry[elements];
        
        // testing practical mem usage
        // for (int i = 0; i < _capacity; i++) {
        //     set(new Entry(i, (byte)0, 0, (byte)0, new Line((short)0)));
        // }
    }

    public static TranspositionTable ofOrder(int order) {
        // This should control the entry capacity, not the size in bytes.
        // This way, we can assume that Zobrist seeds yield similar collision 
        // Counts for different tables with different order.
        return new TranspositionTable((int)Math.pow(2, order));
    }

    public void set(Entry entry) {
        int index = (int) Math.abs(entry.key % _capacity);
        Entry oldEntry = _entries[index];

        if (oldEntry != null) {

            if (entry.key != oldEntry.key) {
                _collisions++;
            }

            if (// Prefer new entries over old ones.
                entry.key != oldEntry.key 
                // Prefer better node types.
                || entry.type < oldEntry.type 
                // Prefer nodes that have been searched with a greater depth.
                || entry.depth >= oldEntry.depth) {
                _entries[index] = entry;
            }
        }
        else {
            _entries[index] = entry;
            _entryCount++;
        }
    }

    public Entry get(long key) {
        Entry entry = _entries[(int) Math.abs(key % _capacity)];
        if (entry == null || entry.key != key)
            return null;
        return entry;
    }

    public static class Entry {
        /**
         * 0 - PV node (exact)
         * 1 - All node (Upper bound)
         * 2 - Cut node (Lower bound, beta cutoff)
         */
        public static final int TYPE_PV = 0;
        public static final int TYPE_ALL = 1;
        public static final int TYPE_CUT = 2;

        public long key;
        public byte depth;
        public int score; 
        public byte type;
        public Line pv;
        // TODO: remove the pv linked list from the hash entry. 
        // This can explode in size and memory usage.

        public Entry(long key, byte depth, int score, byte type, Line pv) {
            this.key = key;
            this.depth = depth;
            this.score = score;
            this.type = type;
            this.pv = pv;
        }
    }

}
