package parstools.zubr.lr;

import parstools.zubr.grammar.Rule;
import java.util.List;

/** One canonical LR(1) item; EOF is terminal index -1. */
public class ItemLR1 extends ItemLRk {
    public ItemLR1(Rule rule, int dotPosition, int terminal) {
        super(rule, dotPosition, List.of(terminal));
    }

    public int terminal() { return lookahead().getFirst(); }

    @Override
    public ItemLR1 goto_() { return new ItemLR1(rule, dotPosition + 1, terminal()); }
}
