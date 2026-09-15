package parstools.zubr.lr;

import parstools.zubr.grammar.Rule;
import parstools.zubr.set.Sequence;
import java.util.List;

/** Immutable lookahead word; representation only, not an LR(k) construction algorithm. */
public class ItemLRk extends ItemLR0 {
    private final List<Integer> lookahead;

    public ItemLRk(Rule rule, int dotPosition, Sequence sequence) {
        this(rule, dotPosition, (List<Integer>) sequence);
    }

    protected ItemLRk(Rule rule, int dotPosition, List<Integer> lookahead) {
        super(rule, dotPosition);
        this.lookahead = List.copyOf(lookahead);
    }

    public List<Integer> lookahead() { return lookahead; }

    @Override
    public int hashCode() { return 31 * super.hashCode() + lookahead.hashCode(); }

    @Override
    public boolean equals(Object other) {
        return super.equals(other) && lookahead.equals(((ItemLRk) other).lookahead);
    }

    @Override
    public ItemLRk goto_() { return new ItemLRk(rule, dotPosition + 1, lookahead); }

    @Override
    public String toString() { return "[" + super.toString() + ", " + lookahead + "]"; }
}
