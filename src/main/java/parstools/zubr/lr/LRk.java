package parstools.zubr.lr;

import parstools.zubr.grammar.Grammar;

/** Canonical LR(k), k >= 1, with exact lookahead words and sparse ACTION tries. */
public class LRk extends AbstractLR {
    public LRk(Grammar grammar, int k) {
        super(ReductionPolicy.ITEM_LOOKAHEAD, k);
        new StatesLRk(grammar, k).createStates(this);
    }
}
