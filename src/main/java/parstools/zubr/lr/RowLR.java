package parstools.zubr.lr;

import java.util.*;

/** ACTION keys are immutable terminal words, leaving room for lookahead lengths above one. */
public class RowLR {
    private final Map<List<Integer>, Set<Action>> action = new LinkedHashMap<>();
    private final Map<Integer, Integer> goto_ = new LinkedHashMap<>();

    void addAction(List<Integer> lookahead, Action value) {
        action.computeIfAbsent(List.copyOf(lookahead), key -> new LinkedHashSet<>()).add(value);
    }

    void addGoto(int nonterminal, int state) {
        Integer old = goto_.putIfAbsent(nonterminal, state);
        if (old != null && old != state)
            throw new IllegalStateException("Inconsistent GOTO targets");
    }

    public Set<Action> actions(int terminal) { return actions(List.of(terminal)); }

    public Set<Action> actions(List<Integer> lookahead) {
        return Collections.unmodifiableSet(action.getOrDefault(lookahead, Set.of()));
    }

    public Map<List<Integer>, Set<Action>> actions() {
        Map<List<Integer>, Set<Action>> result = new LinkedHashMap<>();
        action.forEach((key, value) -> result.put(key, Set.copyOf(value)));
        return Collections.unmodifiableMap(result);
    }

    /** Returns null for an absent transition. */
    public Integer gotoState(int nonterminal) { return goto_.get(nonterminal); }

    public Map<Integer, Integer> gotos() { return Collections.unmodifiableMap(goto_); }

    @Override
    public String toString() { return "ACTION " + action + " GOTO " + goto_; }
}
