package parstools.zubr.lr;

import parstools.zubr.grammar.*;
import parstools.zubr.set.SetContainer;
import java.util.*;

/** Canonical collection shared by LR variants. Hash collisions are resolved by item equality. */
public class States extends ArrayList<State> {
    final Grammar grammar;
    final SetContainer sc;
    final Rule startRule;

    public States(Grammar grammar) {
        if (grammar.nonterminals.isEmpty())
            throw new IllegalArgumentException("A grammar needs a start nonterminal");
        this.grammar = grammar;
        sc = new SetContainer(grammar);
        startRule = grammar.addStartNt().rules.getFirst();
    }

    protected State newState() { return new StateLR0(this); }

    /** Union exact item contexts and remap transitions, preserving the canonical collection. */
    void mergeCores(List<State> canonical) {
        clear();
        Map<Set<ItemLR0>, Integer> groups = new LinkedHashMap<>();
        int[] target = new int[canonical.size()];
        for (int i = 0; i < target.length; i++) {
            State source = canonical.get(i);
            Integer group = groups.get(source.core());
            if (group == null) {
                group = size();
                groups.put(source.core(), group);
                add(newState());
            }
            target[i] = group;
            for (ItemLR0 item : source.items()) get(group).add(item);
        }
        for (int i = 0; i < target.length; i++) {
            State state = get(target[i]);
            canonical.get(i).transitions.forEach((symbol, oldTarget) -> {
                Integer previous = state.transitions.putIfAbsent(symbol, target[oldTarget]);
                if (previous != null && previous != target[oldTarget])
                    throw new IllegalStateException("Inconsistent transitions between merged cores");
            });
        }
    }

    protected void createStates(AbstractLR parser, State initial) {
        clear();
        initial.closure();
        add(initial);
        Map<Set<ItemLR0>, Integer> indices = new HashMap<>();
        indices.put(Set.copyOf(initial.items()), 0);
        List<Symbol> symbols = new ArrayList<>(grammar.nonterminals);
        symbols.addAll(grammar.terminals);
        for (int i = 0; i < size(); i++) {
            State state = get(i);
            for (Symbol symbol : symbols) {
                State target = state.goto_(symbol);
                if (target == null) continue;
                Set<ItemLR0> key = Set.copyOf(target.items());
                Integer index = indices.get(key);
                if (index == null) {
                    index = size();
                    indices.put(key, index);
                    add(target);
                }
                state.transitions.put(symbol, index);
            }
        }
        parser.install(this);
    }
}
