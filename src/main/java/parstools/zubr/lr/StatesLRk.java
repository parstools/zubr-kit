package parstools.zubr.lr;

import parstools.zubr.grammar.Grammar;
import parstools.zubr.grammar.Rule;
import parstools.zubr.set.Sequence;
import parstools.zubr.set.SequenceSet;
import parstools.zubr.set.TokenSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Canonical LR(k) states with local FIRST_k(suffix · lookahead) contexts. */
public class StatesLRk extends States {
    final int k;
    private final Map<ItemLR0, TokenSet> suffixFirst = new HashMap<>();

    public StatesLRk(Grammar grammar, int k) {
        super(requirePositiveK(grammar, k));
        this.k = k;
    }

    private static Grammar requirePositiveK(Grammar grammar, int k) {
        if (k < 1) throw new IllegalArgumentException("k must be positive");
        return grammar;
    }

    void prepareFirstSets() {
        suffixFirst.clear();
        sc.reset(k);
        sc.makeFirstSetsK(k);
    }

    /** Cached suffix sets retain BUILD words until concatenation with the item's context. */
    SequenceSet firstAfter(Rule rule, int dot, List<Integer> lookahead) {
        TokenSet suffix = suffixFirst.computeIfAbsent(new ItemLR0(rule, dot), key -> {
            TokenSet result = new TokenSet(grammar, k);
            sc.addFirstOfRuleK(result, k, rule, dot);
            return result;
        });
        TokenSet context = new TokenSet(grammar, k);
        Sequence word = new Sequence(grammar);
        word.addAll(lookahead);
        if (word.isEof() && word.size() < k) context.addSeqEof(word);
        else context.addSeqDone(word);
        return suffix.concat(context).getPrefixes(k);
    }

    @Override
    protected State newState() { return new StateLRk(this); }

    public void createStates(AbstractLR parser) {
        prepareFirstSets();
        State state = newState();
        state.add(new ItemLRk(startRule, 0, List.of(-1)));
        super.createStates(parser, state);
    }
}
