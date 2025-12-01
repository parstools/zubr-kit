package parstools.zubr.lr;

import parstools.zubr.grammar.Nonterminal;
import parstools.zubr.grammar.Rule;
import parstools.zubr.grammar.Symbol;

import static java.util.Objects.hash;

public class ItemLR0 {
    Rule rule;
    int dotPosition;

    ItemLR0(Rule rule, int dotPosition) {
        this.rule = rule;
        this.dotPosition = dotPosition;
    }

    @Override
    public int hashCode() {
        return hash(rule) ^ hash(dotPosition);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return rule == ((ItemLR0)o).rule && dotPosition == ((ItemLR0)o).dotPosition;
    }

    public Symbol symbolAfterDot() {
        assert(dotPosition <= rule.size());
        if (dotPosition == rule.size())
            return null;
        return rule.get(dotPosition);
    }

    public Nonterminal NtAfterDot() {
        Symbol symbol = symbolAfterDot();
        if (symbol != null && !symbol.terminal)
            return (Nonterminal) symbol;
        else
            return null;
    }

    public ItemLR0 goto_() {
        ItemLR0 result = new ItemLR0(rule, dotPosition + 1);
        return result;
    }
}
