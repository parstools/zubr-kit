package parstools.zubr.lr;

import parstools.zubr.grammar.Rule;
import java.util.HashSet;

public class StateLR0 extends State {
    StateLR0(States owner) { super(owner); }

    @Override
    protected State newState() { return new StateLR0(owner); }

    @Override
    void add(HashSet<ItemLR0> additions, Rule rule, ItemLR0 from) {
        additions.add(new ItemLR0(rule, 0));
    }
}
