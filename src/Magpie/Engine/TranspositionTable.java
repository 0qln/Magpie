package Engine;

import Engine.AlphaBetaSearchTree.Line;

public class TranspositionTable {
    private final Entry[] _entries;
    private final int _elements;

    public TranspositionTable(int elements) {
        _elements = elements;
        _entries = new Entry[elements];
    }

    // size in bytes
    public static TranspositionTable ofSize(int size) {
        return new TranspositionTable(size / Entry.SIZE_BYTES);
    }

    public void set(Entry entry) {
        int index = (int) Math.abs(entry.key % _elements);
        Entry oldEntry = _entries[index];

        if (oldEntry != null) {
            if (entry.type < oldEntry.type || entry.depth >= oldEntry.depth) {
                _entries[index] = entry;
            }
        }
        else {
            _entries[index] = entry;
        }
    }

    public Entry get(long key) {
        Entry entry = _entries[(int) Math.abs(key % _elements)];
        if (entry == null || entry.key != key)
            return null;
        return entry;
    }

    public static class Entry {
        // ~16 Bytes
        public static final byte SIZE_BYTES = 16;

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

        public Entry(long key, byte depth, int score, byte type, Line pv) {
            this.key = key;
            this.depth = depth;
            this.score = score;
            this.type = type;
            this.pv = pv;
        }
    }

}
