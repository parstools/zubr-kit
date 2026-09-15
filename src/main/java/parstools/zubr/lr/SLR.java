package parstools.zubr.lr;

import parstools.zubr.grammar.Grammar;

/** SLR(1): LR(0) states with reductions restricted by FOLLOW of the production owner. */
public class SLR extends AbstractLR {
    public SLR(Grammar grammar) {
        super(ReductionPolicy.FOLLOW);
        new StatesLR0(grammar).createStates(this);
    }
}
