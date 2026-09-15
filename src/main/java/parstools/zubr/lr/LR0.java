package parstools.zubr.lr;

import parstools.zubr.grammar.Grammar;

public class LR0 extends AbstractLR {
    public LR0(Grammar grammar) {
        super(ReductionPolicy.ALL_TERMINALS);
        new StatesLR0(grammar).createStates(this);
    }
}
