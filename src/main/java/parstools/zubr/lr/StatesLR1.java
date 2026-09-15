package parstools.zubr.lr;

import parstools.zubr.grammar.Grammar;
import parstools.zubr.grammar.Nonterminal;
import parstools.zubr.set.Sequence;
import parstools.zubr.set.SetContainer;
import parstools.zubr.set.TokenSet;

public class StatesLR1 extends States {
    StatesLR1(Grammar g) {
        super(g);
    }
    public void createStates(AbstractLR abstractLR) {
        Nonterminal startNt = grammar.addStartNt();
        TokenSet ts = new TokenSet(grammar,1);
        ts.addSeqEof(new Sequence(grammar, "$"));
        ItemLR1 item = new ItemLR1(startNt.rules.getFirst(), 0, ts);
        State state = new StateLR1(this);
        state.add(item);
        sc.reset(1);
        sc.makeFirstSets1();
        sc.makeFollowSets1();
        super.createStates(abstractLR, state);
    }
}
