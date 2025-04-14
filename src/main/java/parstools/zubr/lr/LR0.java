package parstools.zubr.lr;

import parstools.zubr.grammar.Grammar;

public class LR0 extends AbstractLR {
    LR0(Grammar g) {
        StatesLR0 states = new StatesLR0(g);
        states.createStates(this);
    }
}
