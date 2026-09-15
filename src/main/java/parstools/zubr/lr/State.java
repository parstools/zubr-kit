package parstools.zubr.lr;

import parstools.zubr.grammar.Grammar;
import parstools.zubr.grammar.Nonterminal;
import parstools.zubr.grammar.Rule;
import parstools.zubr.grammar.Symbol;
import java.util.*;

/** Shared closure/GOTO engine; subclasses supply closure items and the state factory. */
public abstract class State {
    private final Set<ItemLR0> itemSet = new LinkedHashSet<>();
    final Map<Symbol, Integer> transitions = new LinkedHashMap<>();
    final States owner;
    final Grammar grammar;

    State(States owner) {
        this.owner = owner;
        this.grammar = owner.grammar;
    }

    public static long ror(long value, int shift) { return Long.rotateRight(value, shift); }

    void closure() {
        boolean changed;
        do {
            HashSet<ItemLR0> additions = new LinkedHashSet<>();
            for (ItemLR0 item : itemSet) {
                Nonterminal nt = item.NtAfterDot();
                if (nt != null)
                    for (Rule rule : nt.rules) add(additions, rule, item);
            }
            changed = itemSet.addAll(additions);
        } while (changed);
    }

    long longHash() {
        long result = 0;
        for (ItemLR0 item : itemSet) result ^= item.longHash();
        return result;
    }

    abstract void add(HashSet<ItemLR0> additions, Rule rule, ItemLR0 from);
    protected abstract State newState();

    public State goto_(Symbol symbol) {
        State result = newState();
        for (ItemLR0 item : itemSet)
            if (item.symbolAfterDot() == symbol) result.add(item.goto_());
        if (result.size() == 0) return null;
        result.closure();
        return result;
    }

    public int size() { return itemSet.size(); }
    protected void add(ItemLR0 item) { itemSet.add(item); }
    public Set<ItemLR0> items() { return Collections.unmodifiableSet(itemSet); }

    public Set<ItemLR0> core() {
        Set<ItemLR0> result = new HashSet<>();
        for (ItemLR0 item : itemSet) result.add(item.core());
        return Set.copyOf(result);
    }

    public Map<Symbol, Integer> transitions() {
        return Collections.unmodifiableMap(transitions);
    }
}
