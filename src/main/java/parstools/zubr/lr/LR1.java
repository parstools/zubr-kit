package parstools.zubr.lr;

import parstools.zubr.grammar.Grammar;

public class LR1 extends AbstractLR {
    LR1(Grammar g) {
        StatesLR1 states = new StatesLR1(g);
        states.createStates(this);
    }
}
