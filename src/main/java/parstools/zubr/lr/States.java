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
