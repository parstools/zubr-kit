package parstools.zubr.lr;

import parstools.zubr.grammar.Grammar;

/** LALR(1) by merging canonical LR(1) states with equal LR(0) cores. */
public class LALR extends AbstractLR {
    public LALR(Grammar grammar) { this(new LR1(grammar)); }

    public LALR(LR1 parser) {
        super(ReductionPolicy.ITEM_LOOKAHEAD);
        States merged = new StatesLR1(parser.grammar());
        merged.mergeCores(parser.states());
        install(merged);
    }

    /** Compatibility entry point; rebuilds canonical LR(1), not direct LR(0) propagation. */
    public LALR(LR0 parser) { this(parser.grammar()); }
}
