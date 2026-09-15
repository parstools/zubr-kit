package parstools.zubr.lr;

import parstools.zubr.grammar.Grammar;

public class LR1 extends AbstractLR {
    public LR1(Grammar grammar) {
        super(ReductionPolicy.ITEM_LOOKAHEAD);
        new StatesLR1(grammar).createStates(this);
    }
}
