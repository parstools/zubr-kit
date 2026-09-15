package parstools.zubr.lr;

import parstools.zubr.grammar.Rule;
import parstools.zubr.set.Sequence;
import parstools.zubr.set.TokenSet;
import java.util.HashSet;

public class StateLR1 extends State {
    StateLR1(States owner) { super(owner); }

    @Override
    protected State newState() { return new StateLR1(owner); }

    @Override
    void add(HashSet<ItemLR0> additions, Rule rule, ItemLR0 from) {
        // [A → α · B β, a] adds [B → · γ, b] for each b in FIRST(βa).
        TokenSet first = new TokenSet(grammar, 1);
        owner.sc.addFirstOfRule1(first, from.rule, from.dotPosition + 1);
        for (Sequence word : first.getPrefixes(1))
            additions.add(new ItemLR1(rule, 0, word.getFirst()));
        if (first.hasEpsilon())
            additions.add(new ItemLR1(rule, 0, ((ItemLR1) from).terminal()));
    }
}
