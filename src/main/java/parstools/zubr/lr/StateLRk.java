package parstools.zubr.lr;

import parstools.zubr.grammar.Rule;
import java.util.HashSet;

/** Extension point: LR(k) closure is intentionally not implemented yet. */
public class StateLRk extends State {
    StateLRk(States owner) { super(owner); }

    @Override
    protected State newState() { return new StateLRk(owner); }

    @Override
    void add(HashSet<ItemLR0> additions, Rule rule, ItemLR0 from) {
        throw new UnsupportedOperationException("LR(k) closure is not implemented");
    }

    @Override
    void closure() {
        throw new UnsupportedOperationException("LR(k) closure is not implemented");
    }
}
