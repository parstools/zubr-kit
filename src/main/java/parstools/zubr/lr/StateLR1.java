package parstools.zubr.lr;

import parstools.zubr.grammar.Grammar;
import parstools.zubr.grammar.Rule;
import parstools.zubr.set.TokenSet;

import java.util.HashSet;

public class StateLR1 extends State {
    StateLR1(States owner) {
        super(owner);
    }

    @Override
    void add(HashSet<ItemLR0> newItems, Rule rule, ItemLR0 itemFrom) {
        ItemLR1 itemLR1  =  (ItemLR1)itemFrom;
        TokenSet ts;
//        owner.sc.addFirstOfRule1(ts, rule, )
        ItemLR0 newItem = new ItemLR1(rule, 0, itemLR1.ts);
        newItems.add(newItem);
    }
}
