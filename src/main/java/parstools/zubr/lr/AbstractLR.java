package parstools.zubr.lr;

import parstools.zubr.grammar.*;
import parstools.zubr.set.Sequence;
import parstools.zubr.set.SetContainer;
import java.util.*;

/** Shared table construction and deterministic recognizer for the implemented LR variants. */
public class AbstractLR {
    protected enum ReductionPolicy { ALL_TERMINALS, FOLLOW, ITEM_LOOKAHEAD }
    private final ReductionPolicy policy;
    private final int lookaheadLength;
    private final List<RowLR> rows = new ArrayList<>();
    private final List<Rule> rules = new ArrayList<>();
    private final Map<Rule, Integer> ruleNumbers = new IdentityHashMap<>();
    private States states;

    public AbstractLR() { this(ReductionPolicy.ALL_TERMINALS); }
    protected AbstractLR(ReductionPolicy policy) { this(policy, 1); }
    protected AbstractLR(ReductionPolicy policy, int lookaheadLength) {
        if (lookaheadLength < 1) throw new IllegalArgumentException("k must be positive");
        this.policy = policy;
        this.lookaheadLength = lookaheadLength;
    }

    public int lookaheadLength() { return lookaheadLength; }

    public RowLR row(int index) { return rows.get(index); }
    public List<State> states() { return Collections.unmodifiableList(states); }
    public int stateCount() { return rows.size(); }
    public Grammar grammar() { return states.grammar; }
    public List<Rule> rules() { return Collections.unmodifiableList(rules); }
    public Rule rule(int number) { return rules.get(number); }

    public int ruleNumber(Rule rule) {
        Integer result = ruleNumbers.get(rule);
        if (result == null) throw new IllegalArgumentException("Unknown production");
        return result;
    }

    void install(States collection) {
        states = collection;
        rows.clear();
        rules.clear();
        ruleNumbers.clear();
        rules.add(collection.startRule);
        for (Nonterminal nt : grammar().nonterminals) rules.addAll(nt.rules);
        for (int i = 0; i < rules.size(); i++) ruleNumbers.put(rules.get(i), i);
        SetContainer sets = collection.sc;
        if (policy == ReductionPolicy.FOLLOW) {
            sets.reset(1);
            sets.makeFirstSets1();
            sets.makeFollowSets1();
        }
        for (State state : states) {
            RowLR row = new RowLR();
            rows.add(row);
            state.transitions.forEach((symbol, target) -> {
                if (symbol.terminal) {
                    if (lookaheadLength == 1)
                        row.addAction(List.of(symbol.getIndex()), Action.shift(target));
                }
                else row.addGoto(symbol.getIndex(), target);
            });
            for (ItemLR0 item : state.items()) {
                if (lookaheadLength > 1 && item.symbolAfterDot() instanceof Terminal terminal) {
                    // A shift is enabled by FIRST_k(aβu), not every word starting with a.
                    ItemLRk context = (ItemLRk) item;
                    int target = state.transitions.get(terminal);
                    for (Sequence word : ((StatesLRk) collection).firstAfter(
                            item.rule, item.dotPosition, context.lookahead()))
                        row.addAction(word, Action.shift(target));
                }
                if (!item.completed()) continue;
                if (item.rule == collection.startRule) {
                    row.addAction(List.of(-1), Action.accept());
                    continue;
                }
                Action reduction = Action.reduce(ruleNumber(item.rule));
                switch (policy) {
                    case ALL_TERMINALS -> {
                        for (int token = -1; token < grammar().terminals.size(); token++)
                            row.addAction(List.of(token), reduction);
                    }
                    case FOLLOW -> {
                        for (Sequence word : sets.followSets.get(item.rule.owner.getIndex()).getPrefixes(1))
                            row.addAction(word, reduction);
                    }
                    case ITEM_LOOKAHEAD -> row.addAction(((ItemLRk) item).lookahead(), reduction);
                }
            }
        }
    }

    /** Every competing action is preserved; no precedence or arbitrary conflict resolution. */
    public List<Conflict> conflicts() {
        List<Conflict> result = new ArrayList<>();
        for (int i = 0; i < rows.size(); i++)
            result.addAll(rows.get(i).conflicts(i));
        return List.copyOf(result);
    }

    public boolean isConflictFree() { return rows.stream().noneMatch(RowLR::hasConflicts); }

    /**
     * Recognizes a list of terminal names (for example List.of("id", "+", "id")).
     * EOF is implicit. Conflicting tables are rejected instead of choosing an action.
     */
    public boolean accepts(List<String> tokens) {
        if (!isConflictFree()) throw new IllegalStateException("Parsing table contains conflicts");
        List<Integer> input = new ArrayList<>();
        for (String name : tokens) {
            Terminal terminal = grammar().findT(name);
            if (terminal == null) return false;
            input.add(terminal.getIndex());
        }
        input.add(-1);
        List<Integer> stack = new ArrayList<>();
        stack.add(0);
        int position = 0;
        while (true) {
            Set<Action> cell = row(stack.getLast()).actionsAt(input, position, lookaheadLength);
            if (cell.isEmpty()) return false;
            Action action = cell.iterator().next();
            switch (action.kind()) {
                case SHIFT -> {
                    stack.add(action.number());
                    position++;
                }
                case REDUCTION -> {
                    Rule rule = rule(action.number());
                    for (int i = 0; i < rule.size(); i++) stack.removeLast();
                    Integer next = row(stack.getLast()).gotoState(rule.owner.getIndex());
                    if (next == null) throw new IllegalStateException("Missing GOTO after reduction");
                    stack.add(next);
                }
                case ACCEPT -> { return position == input.size() - 1; }
                case ERROR -> { return false; }
            }
        }
    }
}
