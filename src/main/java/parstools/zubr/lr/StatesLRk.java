package parstools.zubr.lr;

import parstools.zubr.grammar.Grammar;

/** Reserved for future LR(k) construction. */
public class StatesLRk extends States {
    final int k;

    public StatesLRk(Grammar grammar, int k) {
        super(grammar);
        if (k < 1) throw new IllegalArgumentException("k must be positive");
        this.k = k;
    }

    public void createStates(AbstractLR parser) {
        throw new UnsupportedOperationException("LR(k) construction is not implemented");
    }
}
