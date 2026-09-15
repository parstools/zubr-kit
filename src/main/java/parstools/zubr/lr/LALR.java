package parstools.zubr.lr;

import parstools.zubr.grammar.Grammar;
import java.util.*;

/** LALR(1) by merging canonical LR(1) states with equal LR(0) cores. */
public class LALR extends AbstractLR {
    public LALR(Grammar grammar) { this(new LR1(grammar)); }

    public LALR(LR1 parser) {
        super(ReductionPolicy.ITEM_LOOKAHEAD);
        States merged = new States(parser.grammar());
        Map<Set<ItemLR0>, Integer> groups = new LinkedHashMap<>();
        int[] target = new int[parser.stateCount()];
        for (int i = 0; i < target.length; i++) {
            State source = parser.states().get(i);
            Integer group = groups.get(source.core());
            if (group == null) {
                group = merged.size();
                groups.put(source.core(), group);
                merged.add(new StateLR1(merged));
            }
            target[i] = group;
            for (ItemLR0 item : source.items()) merged.get(group).add(item);
        }
        for (int i = 0; i < target.length; i++) {
            State state = merged.get(target[i]);
            parser.states().get(i).transitions.forEach((symbol, oldTarget) -> {
                Integer previous = state.transitions.putIfAbsent(symbol, target[oldTarget]);
                if (previous != null && previous != target[oldTarget])
                    throw new IllegalStateException("Inconsistent transitions between merged cores");
            });
        }
        install(merged);
    }

    /** Compatibility entry point; rebuilds canonical LR(1), not direct LR(0) propagation. */
    public LALR(LR0 parser) { this(parser.grammar()); }
}
