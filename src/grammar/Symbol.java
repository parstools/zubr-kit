package grammar;

import util.Hash;

public class Symbol {
    public final boolean terminal;
    public final int index;
    private Grammar grammar;
    public Symbol(Grammar grammar, boolean terminal, int index) {
        this.grammar = grammar;
        this.terminal = terminal;
        this.index = index;
    }

    @Override
    public String toString() {
        return grammar.getSymbolName(this);
    }

    @Override
    public int hashCode() {
        Hash h = new Hash();
        h.add(terminal?1:0);
        h.add(index);
        return h.hash();
    }
}
