package parstools.zubr.lr;

import parstools.zubr.grammar.Grammar;
import parstools.zubr.grammar.Nonterminal;
import parstools.zubr.set.Sequence;
import parstools.zubr.set.SetContainer;

public class StatesLRk extends States{
    int k;
    StatesLRk(Grammar grammar, int k) {
        super(grammar);
        this.k = k;
    }

    public void createStates(AbstractLR abstractLR) {
        Nonterminal startNt = grammar.addStartNt();
        Sequence sequence = new Sequence(grammar);
        sequence.add(-1);
        ItemLRk item = new ItemLRk(startNt.rules.get(0), 0, sequence);
        State state = new StateLRk(grammar);
        SetContainer sc = new SetContainer(grammar);
        sc.reset(k);
        sc.makeFirstSetsK(k);
        sc.makeFollowSetsK(k);
        super.createStates(abstractLR, state);
    }
}
