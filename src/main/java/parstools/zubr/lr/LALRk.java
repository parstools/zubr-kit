package parstools.zubr.lr;

import parstools.zubr.grammar.Grammar;

/** Reserved for future LALR(k) construction. */
public class LALRk extends AbstractLR {
    public LALRk(Grammar grammar) {
        throw new UnsupportedOperationException("LALR(k) construction is not implemented");
    }

    public LALRk(Grammar grammar, int k) {
        throw new UnsupportedOperationException("LALR(k) construction is not implemented");
    }
}
