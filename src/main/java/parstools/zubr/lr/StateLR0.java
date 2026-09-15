package parstools.zubr.lr;

import parstools.zubr.grammar.Grammar;
import parstools.zubr.grammar.Rule;

import java.util.HashSet;

public class StateLR0 extends State {

    StateLR0(States owner) {
        super(owner);
    }

    @Override
    void add(HashSet<ItemLR0> newItems, Rule rule, ItemLR0 itemFrom) {
        ItemLR0 newItem = new ItemLR0(rule, 0);
        newItems.add(newItem);
    }
}
