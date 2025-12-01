package parstools.zubr.util;

public final class HashBuilder64 {
    private long h = 0L;

    // ten sam magiczny współczynnik co w C++ (64-bit golden ratio)
    private static final long MAGIC = 0x9e3779b97f4a7c15L;

    public HashBuilder64() {}

    public long value() {
        return h;
    }

    public void reset() {
        h = 0L;
    }

    public void addInt(int v) {
        long hv = Integer.hashCode(v);
        mix(hv);
    }

    public void addLong(long v) {
        long hv = Long.hashCode(v);
        mix(hv);
    }

    public void addBool(boolean v) {
        long hv = Boolean.hashCode(v);
        mix(hv);
    }

    public void addObject(Object o) {
        // używamy hashCode() obiektu, ale jako 64 bitów
        long hv = (o == null ? 0 : o.hashCode());
        mix(hv);
    }

    public void addString(String s) {
        long hv = (s == null ? 0 : s.hashCode());
        mix(hv);
    }

    private void mix(long hv) {
        h ^= hv + MAGIC + (h << 6) + (h >>> 2);
    }
}
