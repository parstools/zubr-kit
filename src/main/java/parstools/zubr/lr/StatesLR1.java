package parstools.zubr.lr;

import parstools.zubr.grammar.Grammar;
import parstools.zubr.grammar.Nonterminal;
import parstools.zubr.set.SetContainer;

public class StatesLR1 extends States {
    StatesLR1(Grammar g) {
        super(g);
    }

    public void createStates(AbstractLR abstractLR) {
        Nonterminal startNt = grammar.addStartNt();
        ItemLR1 item = new ItemLR1(startNt.rules.get(0), 0, Grammar.EOF);
        State state = new StateLR1(grammar);
        SetContainer sc = new SetContainer(grammar);
        sc.reset(1);
        sc.makeFirstSets1();
        sc.makeFollowSets1();
        super.createStates(abstractLR, state);
    }
}
