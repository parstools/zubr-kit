package parstools.zubr.grammar;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public abstract class Symbol {
    public final boolean terminal;
    protected Grammar grammar;
    public String name;
    public int minLen;
    public int maxLen;

    public static int compare(Symbol symbol0, Symbol symbol1) {
        if (symbol0.terminal!=symbol1.terminal) {
          if (symbol0.terminal)
              return 1;
          else
              return -1;
        }
        return Integer.compare(symbol0.getIndex(), symbol1.getIndex());
    }

    public abstract int getIndex();

    public Symbol(Grammar grammar, boolean terminal, String name) {
        this.grammar = grammar;
        this.terminal = terminal;
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    protected byte[] getBytes() {
        byte[] stringBytes = name.getBytes(StandardCharsets.UTF_8);
        ByteBuffer buffer = ByteBuffer.allocate(stringBytes.length + 1);
        buffer.put(stringBytes);
        byte b = (byte) (terminal? 1: 0);
        buffer.put(b);
        return buffer.array();
    }
}
