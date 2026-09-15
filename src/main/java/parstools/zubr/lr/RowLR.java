package parstools.zubr.lr;

import parstools.zubr.set.SortedIntMap;
import java.util.*;

/** Sparse ACTION trie. Only used lookahead words have paths; no alphabet cross product. */
public class RowLR {
    /** Read-only cursor for inspecting a decision one terminal at a time. */
    public static final class ActionNode {
        private final SortedIntMap<ActionNode> children = new SortedIntMap<>(2);
        private Set<Action> actions;

        public ActionNode next(int terminal) { return children.get(terminal); }
        public Set<Integer> nextTokens() { return Collections.unmodifiableSet(children.keySet()); }
        public Set<Action> actions() {
            return actions == null ? Set.of() : Collections.unmodifiableSet(actions);
        }
    }

    private final ActionNode root = new ActionNode();
    private final Map<Integer, Integer> goto_ = new LinkedHashMap<>();
    private int nodeCount = 1;
    private int entryCount;
    private int conflictCount;

    public ActionNode actionRoot() { return root; }
    public int actionNodeCount() { return nodeCount; }
    public int actionEntryCount() { return entryCount; }
    public boolean hasConflicts() { return conflictCount != 0; }

    void addAction(List<Integer> lookahead, Action value) {
        if (lookahead.isEmpty()) throw new IllegalArgumentException("Empty lookahead");
        for (int i = 0; i < lookahead.size(); i++) {
            int token = lookahead.get(i);
            if (token < -1 || token == -1 && i != lookahead.size() - 1)
                throw new IllegalArgumentException("EOF must terminate the lookahead");
        }
        ActionNode node = root;
        for (int token : lookahead) {
            ActionNode child = node.children.get(token);
            if (child == null) {
                child = new ActionNode();
                node.children.put(token, child);
                nodeCount++;
            }
            node = child;
        }
        if (node.actions == null) {
            node.actions = new LinkedHashSet<>();
            entryCount++;
        }
        if (node.actions.add(value) && node.actions.size() == 2) conflictCount++;
    }

    void addGoto(int nonterminal, int state) {
        Integer old = goto_.putIfAbsent(nonterminal, state);
        if (old != null && old != state)
            throw new IllegalStateException("Inconsistent GOTO targets");
    }

    /** Exact one-token key, not the union of all words starting with that token. */
    public Set<Action> actions(int terminal) { return actions(List.of(terminal)); }

    public Set<Action> actions(List<Integer> lookahead) {
        ActionNode node = root;
        for (int token : lookahead) {
            node = node.next(token);
            if (node == null) return Set.of();
        }
        return node.actions();
    }

    /** Follows at most k edges, stopping at EOF or a missing edge; no tuple allocation. */
    Set<Action> actionsAt(List<Integer> input, int offset, int k) {
        ActionNode node = root;
        for (int i = 0; i < k; i++) {
            if (offset + i >= input.size()) return Set.of();
            int token = input.get(offset + i);
            node = node.next(token);
            if (node == null) return Set.of();
            if (token == -1) return node.actions();
        }
        return node.actions();
    }

    /** Materializes only populated cells, for diagnostics and compatibility. */
    public Map<List<Integer>, Set<Action>> actions() {
        Map<List<Integer>, Set<Action>> result = new LinkedHashMap<>();
        visit(root, new ArrayList<>(), (word, actions) -> result.put(word, Set.copyOf(actions)), false);
        return Collections.unmodifiableMap(result);
    }

    List<Conflict> conflicts(int state) {
        List<Conflict> result = new ArrayList<>();
        if (hasConflicts())
            visit(root, new ArrayList<>(), (word, actions) ->
                    result.add(new Conflict(state, word, actions)), true);
        return result;
    }

    private static void visit(ActionNode node, List<Integer> path,
                              java.util.function.BiConsumer<List<Integer>, Set<Action>> consumer,
                              boolean conflictsOnly) {
        if (node.actions != null && (!conflictsOnly || node.actions.size() > 1))
            consumer.accept(List.copyOf(path), node.actions);
        for (int token : node.children.keySet()) {
            path.add(token);
            visit(node.next(token), path, consumer, conflictsOnly);
            path.removeLast();
        }
    }

    /** Returns null for an absent transition. */
    public Integer gotoState(int nonterminal) { return goto_.get(nonterminal); }
    public Map<Integer, Integer> gotos() { return Collections.unmodifiableMap(goto_); }

    @Override
    public String toString() { return "ACTION " + actions() + " GOTO " + goto_; }
}
