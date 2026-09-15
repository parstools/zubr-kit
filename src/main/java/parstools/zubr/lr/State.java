package parstools.zubr.lr;

import parstools.zubr.grammar.Grammar;
import parstools.zubr.grammar.Nonterminal;
import parstools.zubr.grammar.Rule;
import parstools.zubr.grammar.Symbol;
import parstools.zubr.util.HashBuilder64;

import java.util.HashSet;

import static java.io.IO.println;

public abstract class State {
    private HashSet<ItemLR0> itemSet = new HashSet<>();
    States owner;
    Grammar grammar;

    State(States owner) {
        this.owner = owner;
        this.grammar = owner.grammar;
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
    }

    long longHash() {
        //permutations have the same hash
        long result = 0;
        for (ItemLR0 item: itemSet)
            result  ^= item.longHash();
        return result;
    }

    abstract void add(HashSet<ItemLR0> newItems, Rule rule, ItemLR0 itemFrom);

    public State goto_(Symbol symbol) {
        State newState = new StateLR0(owner);
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