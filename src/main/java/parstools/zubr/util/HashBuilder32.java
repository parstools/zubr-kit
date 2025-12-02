package parstools.zubr.util;

public final class HashBuilder32 {
    private int h = 0;

    private static final int MAGIC = 0x9e3779b9;

    public HashBuilder32() {
    }

    public int hash() {
        return h;
    }

    public void reset() {
        h = 0;
    }

    public void addInt(int v) {
        int hv = Integer.hashCode(v);
        mix(hv);
    }

    public void addLong(long v) {
        int hv = Long.hashCode(v);   // standardowa redukcja long -> int
        mix(hv);
    }

    public void addBool(boolean v) {
        int hv = Boolean.hashCode(v);
        mix(hv);
    }

    public void addObject(Object o) {
        int hv = (o == null ? 0 : o.hashCode());
        mix(hv);
    }

    public void addString(String s) {
        int hv = (s == null ? 0 : s.hashCode());
        mix(hv);
    }

    private void mix(int hv) {
        h ^= hv + MAGIC + (h << 6) + (h >>> 2);
    }
}
