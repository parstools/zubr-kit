package parstools.zubr.lr;

import parstools.zubr.grammar.Grammar;

/** LALR(k) by core merging of canonical LR(k). Merging can introduce conflicts. */
public class LALRk extends AbstractLR {
    public LALRk(Grammar grammar) { this(grammar, 1); }
    public LALRk(Grammar grammar, int k) { this(new LRk(grammar, k)); }

    public LALRk(LRk canonical) {
        super(ReductionPolicy.ITEM_LOOKAHEAD, canonical.lookaheadLength());
        StatesLRk merged = new StatesLRk(canonical.grammar(), canonical.lookaheadLength());
        merged.prepareFirstSets();
        merged.mergeCores(canonical.states());
        install(merged);
    }
}
