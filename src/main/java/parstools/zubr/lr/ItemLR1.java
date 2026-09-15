package parstools.zubr.lr;

import parstools.zubr.grammar.Rule;
import parstools.zubr.grammar.Terminal;
import parstools.zubr.set.TokenSet;

public class ItemLR1 extends ItemLR0 {
    TokenSet ts;

    ItemLR1(Rule rule, int dotPosition, TokenSet ts) {
        super(rule, dotPosition);
        this.ts = ts;
    }
}
