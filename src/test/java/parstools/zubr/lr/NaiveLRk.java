package parstools.zubr.lr;

import parstools.zubr.grammar.*;
import java.util.*;

/**
 * Deliberately dense, small-grammar test oracle. Does not use SetContainer,
 * TokenSet, production State/closure code, RowLR, or the production recognizer.
 */
final class NaiveLRk {
    record Item(int rule, int dot, List<Integer> lookahead) {
        Item { lookahead = List.copyOf(lookahead); }
    }

    final Grammar grammar;
    final int k;
    final List<Rule> rules = new ArrayList<>();
    final List<Set<Item>> states = new ArrayList<>();
    final List<Map<Symbol, Integer>> transitions = new ArrayList<>();
    final List<Map<List<Integer>, Set<Action>>> tables = new ArrayList<>();
    final List<List<Integer>> columns = new ArrayList<>();
    private final Map<Nonterminal, Set<List<Integer>>> first = new IdentityHashMap<>();

    NaiveLRk(Grammar grammar, int k) {
        this.grammar = grammar;
        this.k = k;
        rules.add(grammar.addStartNt().rules.getFirst());
        for (Nonterminal nt : grammar.nonterminals) {
            rules.addAll(nt.rules);
            first.put(nt, new HashSet<>());
        }
        boolean changed;
        do {
            changed = false;
            for (int i = 1; i < rules.size(); i++) {
                Rule rule = rules.get(i);
                if (first.get(rule.owner).addAll(suffix(rule, 0))) changed = true;
            }
        } while (changed);
        enumerateColumns(new ArrayList<>());
        Set<Item> initial = closure(Set.of(new Item(0, 0, List.of(-1))));
        states.add(initial);
        List<Symbol> symbols = new ArrayList<>(grammar.nonterminals);
        symbols.addAll(grammar.terminals);
        for (int i = 0; i < states.size(); i++) {
            Map<Symbol, Integer> edges = new LinkedHashMap<>();
            for (Symbol symbol : symbols) {
                Set<Item> kernel = new HashSet<>();
                for (Item item : states.get(i)) {
                    Rule rule = rules.get(item.rule);
                    if (item.dot < rule.size() && rule.get(item.dot) == symbol)
                        kernel.add(new Item(item.rule, item.dot + 1, item.lookahead));
                }
                if (kernel.isEmpty()) continue;
                Set<Item> target = closure(kernel);
                int index = states.indexOf(target); // Intentional linear search, independent of production hashes.
                if (index < 0) {
                    index = states.size();
                    states.add(target);
                }
                edges.put(symbol, index);
            }
            transitions.add(edges);
        }
        buildTables();
    }

    private List<Integer> concat(List<Integer> left, List<Integer> right) {
        List<Integer> result = new ArrayList<>();
        for (List<Integer> part : List.of(left, right))
            for (int token : part) {
                if (result.size() == k || !result.isEmpty() && result.getLast() == -1)
                    return List.copyOf(result);
                result.add(token);
            }
        return List.copyOf(result);
    }

    private Set<List<Integer>> product(Set<List<Integer>> left, Set<List<Integer>> right) {
        Set<List<Integer>> result = new HashSet<>();
        for (List<Integer> prefix : left) {
            if (prefix.size() == k || !prefix.isEmpty() && prefix.getLast() == -1)
                result.add(prefix);
            else
                for (List<Integer> suffix : right) result.add(concat(prefix, suffix));
        }
        return result;
    }

    private Set<List<Integer>> suffix(Rule rule, int dot) {
        Set<List<Integer>> result = Set.of(List.of());
        for (int i = dot; i < rule.size(); i++) {
            Symbol symbol = rule.get(i);
            Set<List<Integer>> next = symbol.terminal
                    ? Set.of(List.of(symbol.getIndex())) : first.get((Nonterminal) symbol);
            result = product(result, next);
        }
        return result;
    }

    private Set<List<Integer>> contexts(Item item, int dot) {
        return product(suffix(rules.get(item.rule), dot), Set.of(item.lookahead));
    }

    private Set<Item> closure(Set<Item> kernel) {
        Set<Item> result = new HashSet<>(kernel);
        boolean changed;
        do {
            Set<Item> additions = new HashSet<>();
            for (Item item : result) {
                Rule rule = rules.get(item.rule);
                if (item.dot == rule.size() || rule.get(item.dot).terminal) continue;
                Symbol next = rule.get(item.dot);
                for (int i = 1; i < rules.size(); i++)
                    if (rules.get(i).owner == next)
                        for (List<Integer> word : contexts(item, item.dot + 1))
                            additions.add(new Item(i, 0, word));
            }
            changed = result.addAll(additions);
        } while (changed);
        return result;
    }

    private void enumerateColumns(List<Integer> prefix) {
        if (prefix.size() == k) {
            columns.add(List.copyOf(prefix));
            return;
        }
        List<Integer> eof = new ArrayList<>(prefix);
        eof.add(-1);
        columns.add(List.copyOf(eof));
        for (int t = 0; t < grammar.terminals.size(); t++) {
            prefix.add(t);
            enumerateColumns(prefix);
            prefix.removeLast();
        }
    }

    private void buildTables() {
        for (int state = 0; state < states.size(); state++) {
            Map<List<Integer>, Set<Action>> row = new LinkedHashMap<>();
            for (List<Integer> word : columns) row.put(word, new HashSet<>());
            for (Item item : states.get(state)) {
                Rule rule = rules.get(item.rule);
                if (item.dot == rule.size()) {
                    row.get(item.lookahead).add(item.rule == 0 ? Action.accept() : Action.reduce(item.rule));
                } else if (rule.get(item.dot).terminal) {
                    Set<List<Integer>> enabled = contexts(item, item.dot);
                    Action shift = Action.shift(transitions.get(state).get(rule.get(item.dot)));
                    for (List<Integer> column : columns)
                        if (enabled.contains(column)) row.get(column).add(shift);
                }
            }
            tables.add(row);
        }
    }

    boolean isConflictFree() {
        return tables.stream().flatMap(row -> row.values().stream()).noneMatch(cell -> cell.size() > 1);
    }

    boolean accepts(List<String> names) {
        if (!isConflictFree()) throw new IllegalStateException("Conflicting oracle");
        List<Integer> tokens = new ArrayList<>();
        for (String name : names) {
            Terminal token = grammar.findT(name);
            if (token == null) return false;
            tokens.add(token.getIndex());
        }
        tokens.add(-1);
        List<Integer> stack = new ArrayList<>(List.of(0));
        int position = 0;
        while (true) {
            List<Integer> window = tokens.subList(position, Math.min(tokens.size(), position + k));
            Set<Action> cell = tables.get(stack.getLast()).get(window);
            if (cell == null || cell.isEmpty()) return false;
            Action action = cell.iterator().next();
            if (action.kind() == ActionKind.ACCEPT) return position == tokens.size() - 1;
            if (action.kind() == ActionKind.SHIFT) {
                stack.add(action.number());
                position++;
            } else {
                Rule rule = rules.get(action.number());
                stack.subList(stack.size() - rule.size(), stack.size()).clear();
                stack.add(transitions.get(stack.getLast()).get(rule.owner));
            }
        }
    }
}
