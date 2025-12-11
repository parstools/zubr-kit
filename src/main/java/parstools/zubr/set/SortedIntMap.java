package parstools.zubr.set;

import java.util.*;

public class SortedIntMap<T> {
    private int[] keys;
    private Object[] values;  // Type erasure requires Object[]
    private int size;
    private int capacity;

    public SortedIntMap() {
        this(8);
    }

    public SortedIntMap(int initialCapacity) {
        this.capacity = initialCapacity;
        this.keys = new int[capacity];
        this.values = new Object[capacity];
        this.size = 0;
    }

    /**
     * Inserts a (key, value) pair. If key already exists, overwrites the value.
     * O(n) - insertion sort is optimal for small n
     */
    public void put(int key, T value) {
        int index = binarySearch(key);

        if (index >= 0) {
            // Key already exists - overwrite value
            values[index] = value;
        } else {
            // Key doesn't exist - insert at sorted position
            int insertPos = -(index + 1);

            // Grow array if needed
            if (size == capacity) {
                grow();
            }

            // Shift elements right
            System.arraycopy(keys, insertPos, keys, insertPos + 1, size - insertPos);
            System.arraycopy(values, insertPos, values, insertPos + 1, size - insertPos);

            keys[insertPos] = key;
            values[insertPos] = value;
            size++;
        }
    }

    /**
     * Finds value for key.
     * O(log n)
     */
    @SuppressWarnings("unchecked")
    public T get(int key) {
        int index = binarySearch(key);
        return index >= 0 ? (T) values[index] : null;
    }

    /**
     * Checks if key exists.
     * O(log n)
     */
    public boolean containsKey(int key) {
        return binarySearch(key) >= 0;
    }

    /**
     * Removes key-value pair.
     * O(n)
     */
    public boolean remove(int key) {
        int index = binarySearch(key);
        if (index < 0) {
            return false;
        }

        // Shift elements left
        System.arraycopy(keys, index + 1, keys, index, size - index - 1);
        System.arraycopy(values, index + 1, values, index, size - index - 1);

        size--;
        values[size] = null; // GC

        return true;
    }

    /**
     * Binary search returns:
     * - index >= 0 if found
     * - -(insertion_point + 1) if not found
     */
    private int binarySearch(int key) {
        int low = 0;
        int high = size - 1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            int midVal = keys[mid];

            if (midVal < key) {
                low = mid + 1;
            } else if (midVal > key) {
                high = mid - 1;
            } else {
                return mid; // Found
            }
        }

        return -(low + 1); // Not found, return insertion point
    }

    private void grow() {
        capacity = capacity * 2;
        keys = Arrays.copyOf(keys, capacity);
        values = Arrays.copyOf(values, capacity);
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public void clear() {
        Arrays.fill(values, 0, size, null);
        size = 0;
    }

    // Iterator over keys (already sorted!)
    public int[] keys() {
        return Arrays.copyOf(keys, size);
    }

    // Iterator over values
    @SuppressWarnings("unchecked")
    public T[] values(T[] array) {
        if (array.length < size) {
            return (T[]) Arrays.copyOf(values, size, array.getClass());
        }
        System.arraycopy(values, 0, array, 0, size);
        if (array.length > size) {
            array[size] = null;
        }
        return array;
    }

    // ========================================================================
    // Set views
    // ========================================================================

    /**
     * Returns a Set view of the keys in this map.
     * The set is backed by the map, so changes to the map are reflected in the set.
     * The set supports element removal through Iterator.remove(), but not addition.
     */
    public Set<Integer> keySet() {
        return new KeySet();
    }

    /**
     * Returns a Set view of the entries in this map.
     * The set is backed by the map, so changes to the map are reflected in the set.
     * The set supports element removal through Iterator.remove(), but not addition.
     */
    public Set<Map.Entry<Integer, T>> entrySet() {
        return new EntrySet();
    }

    // ========================================================================
    // KeySet implementation
    // ========================================================================

    private class KeySet extends AbstractSet<Integer> {
        @Override
        public Iterator<Integer> iterator() {
            return new KeyIterator();
        }

        @Override
        public int size() {
            return SortedIntMap.this.size;
        }

        @Override
        public boolean contains(Object o) {
            if (!(o instanceof Integer)) {
                return false;
            }
            return containsKey((Integer) o);
        }

        @Override
        public boolean remove(Object o) {
            if (!(o instanceof Integer)) {
                return false;
            }
            return SortedIntMap.this.remove((Integer) o);
        }

        @Override
        public void clear() {
            SortedIntMap.this.clear();
        }
    }

    private class KeyIterator implements Iterator<Integer> {
        private int cursor = 0;
        private int lastReturned = -1;

        @Override
        public boolean hasNext() {
            return cursor < size;
        }

        @Override
        public Integer next() {
            if (cursor >= size) {
                throw new NoSuchElementException();
            }
            lastReturned = cursor;
            return keys[cursor++];
        }

        @Override
        public void remove() {
            if (lastReturned < 0) {
                throw new IllegalStateException();
            }
            SortedIntMap.this.remove(keys[lastReturned]);
            cursor = lastReturned;
            lastReturned = -1;
        }
    }

    // ========================================================================
    // EntrySet implementation
    // ========================================================================

    private class EntrySet extends AbstractSet<Map.Entry<Integer, T>> {
        @Override
        public Iterator<Map.Entry<Integer, T>> iterator() {
            return new EntryIterator();
        }

        @Override
        public int size() {
            return SortedIntMap.this.size;
        }

        @Override
        public boolean contains(Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry<?, ?> entry = (Map.Entry<?, ?>) o;
            if (!(entry.getKey() instanceof Integer)) {
                return false;
            }
            int key = (Integer) entry.getKey();
            T value = get(key);
            return value != null && value.equals(entry.getValue());
        }

        @Override
        public boolean remove(Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry<?, ?> entry = (Map.Entry<?, ?>) o;
            if (!(entry.getKey() instanceof Integer)) {
                return false;
            }
            return SortedIntMap.this.remove((Integer) entry.getKey());
        }

        @Override
        public void clear() {
            SortedIntMap.this.clear();
        }
    }

    private class EntryIterator implements Iterator<Map.Entry<Integer, T>> {
        private int cursor = 0;
        private int lastReturned = -1;

        @Override
        public boolean hasNext() {
            return cursor < size;
        }

        @Override
        public Map.Entry<Integer, T> next() {
            if (cursor >= size) {
                throw new NoSuchElementException();
            }
            lastReturned = cursor;
            return new Entry(cursor++);
        }

        @Override
        public void remove() {
            if (lastReturned < 0) {
                throw new IllegalStateException();
            }
            SortedIntMap.this.remove(keys[lastReturned]);
            cursor = lastReturned;
            lastReturned = -1;
        }
    }

    // ========================================================================
    // Entry implementation
    // ========================================================================

    private class Entry implements Map.Entry<Integer, T> {
        private final int index;
        private final int key;

        Entry(int index) {
            this.index = index;
            this.key = keys[index];
        }

        @Override
        public Integer getKey() {
            return key;
        }

        @Override
        @SuppressWarnings("unchecked")
        public T getValue() {
            return (T) values[index];
        }

        @Override
        public T setValue(T value) {
            @SuppressWarnings("unchecked")
            T oldValue = (T) values[index];
            values[index] = value;
            return oldValue;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
            return Objects.equals(key, e.getKey()) &&
                    Objects.equals(getValue(), e.getValue());
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(key) ^ Objects.hashCode(getValue());
        }

        @Override
        public String toString() {
            return key + "=" + getValue();
        }
    }
}