package parstools.zubr.lr;

import parstools.zubr.grammar.Grammar;
import parstools.zubr.grammar.Symbol;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class States extends ArrayList<State> {
    Grammar grammar;
    public States(Grammar grammar) {
        this.grammar = grammar;
    }

    protected void createStates(AbstractLR abstractLR, State state) {
        Set<Long> hashSet = new HashSet<>();
        state.closure();
        add(state);
        List<Symbol> symList = new ArrayList<>();
        symList.addAll(grammar.terminals);
        symList.addAll(grammar.nonterminals);
        int index = 0;
        while(index < size()) {
            state = get(index);
            for (Symbol symbol: symList) {
                State newState = state.goto_(symbol);
                if (newState != null && !hashSet.contains(newState.longHash)) {
                    hashSet.add(newState.longHash);
                    abstractLR.add(index, symbol, size());
                    add(newState);
                }
            }
            index++;
        }
    }
}
