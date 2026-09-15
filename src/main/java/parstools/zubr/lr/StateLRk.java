package parstools.zubr.lr;

import parstools.zubr.grammar.Rule;
import parstools.zubr.set.Sequence;
import java.util.HashSet;

/** Exact LR(k) closure: [A → α · B β, u] adds [B → · γ, FIRST_k(βu)]. */
public class StateLRk extends State {
    StateLRk(StatesLRk owner) { super(owner); }

    @Override
    protected State newState() { return new StateLRk((StatesLRk) owner); }

    @Override
    void add(HashSet<ItemLR0> additions, Rule rule, ItemLR0 from) {
        ItemLRk item = (ItemLRk) from;
        for (Sequence word : ((StatesLRk) owner).firstAfter(item.rule, item.dotPosition + 1, item.lookahead()))
            additions.add(new ItemLRk(rule, 0, word));
    }
}
