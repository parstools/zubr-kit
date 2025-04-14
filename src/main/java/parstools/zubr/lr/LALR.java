package parstools.zubr.lr;

import parstools.zubr.grammar.Grammar;

public class LALR extends AbstractLR {
    boolean efficient;
    LALR(Grammar g) {
        if (efficient) {
            createFromLR0(new LR0(new Grammar()));
        } else {
            createFromLR1(new LR1(new Grammar()));
        }

    }

    private void createFromLR1(LR1 lr1) {

    }

    private void createFromLR0(LR0 lr0) {

    }

    LALR(LR1 parser) {
        createFromLR1(parser);
    }

    LALR(LR0 parser) {
        createFromLR0(parser);
    }
}
