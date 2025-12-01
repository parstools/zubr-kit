package parstools.zubr.lr;

import parstools.zubr.grammar.Grammar;

public class LALRk extends AbstractLR {
    int k;
    LALRk(Grammar g) {
        StatesLRk states = new StatesLRk(g, k);
        states.createStates(this, null);
    }
}
