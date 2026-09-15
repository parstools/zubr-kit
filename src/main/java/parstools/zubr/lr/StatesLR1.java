package parstools.zubr.lr;

import parstools.zubr.grammar.Grammar;

public class StatesLR1 extends States {
    public StatesLR1(Grammar grammar) { super(grammar); }

    public void createStates(AbstractLR parser) {
        sc.reset(1);
        sc.makeFirstSets1();
        State state = new StateLR1(this);
        state.add(new ItemLR1(startRule, 0, -1));
        super.createStates(parser, state);
    }
}
