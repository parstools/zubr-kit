package parstools.zubr.lr;

import parstools.zubr.grammar.Grammar;

public class StatesLR0 extends States {
    public StatesLR0(Grammar grammar) { super(grammar); }

    public void createStates(AbstractLR parser) {
        State state = new StateLR0(this);
        state.add(new ItemLR0(startRule, 0));
        super.createStates(parser, state);
    }
}
