package parstools.zubr.lr;

import parstools.zubr.grammar.Grammar;
import parstools.zubr.grammar.Nonterminal;
import parstools.zubr.grammar.Rule;
import parstools.zubr.grammar.Symbol;

import java.util.HashSet;

public abstract class State {
    private HashSet<ItemLR0> itemSet = new HashSet<>();
    Grammar grammar;
    long longHash = 0;

    State(Grammar grammar) {
        this.grammar = grammar;
    }

    public static long ror(long value, int shift) {
        return (value >>> shift) | (value << (Long.SIZE - shift));
    }

    void closure() {
        boolean isModified;
        do {
            HashSet<ItemLR0> newItems = new HashSet<>();
            for (ItemLR0 item: itemSet) {
                Nonterminal nt = item.NtAfterDot();
                if (nt != null)
                    for (Rule rule: nt.rules)
                        add(newItems, rule, item);
            }
            isModified = itemSet.addAll(newItems);
        } while (isModified);
        longHash = 0;
        for (ItemLR0 item: itemSet)
            longHash = ror(longHash,10) ^  item.hashCode();
    }

    abstract void add(HashSet<ItemLR0> newItems, Rule rule, ItemLR0 itemFrom);

    public State goto_(Symbol symbol) {
        State newState = new StateLR0(grammar);
        for (ItemLR0 item: itemSet) {
            Symbol symbol1 = item.symbolAfterDot();
            if (symbol1 == symbol)
                newState.add(item.goto_());
        }
        if (newState.size() > 0) {
            newState.closure();
            return newState;
        } else
            return null;
    }

    private int size() {
        return itemSet.size();
    }

    protected void add(ItemLR0 item) {
        itemSet.add(item);
    }
}