package parstools.zubr.lr;

import parstools.zubr.grammar.Nonterminal;
import parstools.zubr.grammar.Rule;
import parstools.zubr.grammar.Symbol;

/** Immutable production/dot pair. Production identity distinguishes duplicate rules. */
public class ItemLR0 {
    public final Rule rule;
    public final int dotPosition;

    public ItemLR0(Rule rule, int dotPosition) {
        if (dotPosition < 0 || dotPosition > rule.size())
            throw new IllegalArgumentException("Dot outside production");
        this.rule = rule;
        this.dotPosition = dotPosition;
    }

    @Override
    public int hashCode() {
        return 31 * System.identityHashCode(rule) + dotPosition;
    }

    long longHash() { return hashCode(); }

    @Override
    public boolean equals(Object other) {
        return other != null && getClass() == other.getClass()
                && rule == ((ItemLR0) other).rule
                && dotPosition == ((ItemLR0) other).dotPosition;
    }

    public ItemLR0 core() { return new ItemLR0(rule, dotPosition); }
    public boolean completed() { return dotPosition == rule.size(); }
    public Symbol symbolAfterDot() { return completed() ? null : rule.get(dotPosition); }

    public Nonterminal NtAfterDot() {
        return symbolAfterDot() instanceof Nonterminal nt ? nt : null;
    }

    public ItemLR0 goto_() { return new ItemLR0(rule, dotPosition + 1); }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder(rule.owner.name.isEmpty() ? "S′" : rule.owner.name);
        out.append(" →");
        for (int i = 0; i <= rule.size(); i++) {
            if (i == dotPosition) out.append(" ·");
            if (i < rule.size()) out.append(" ").append(rule.get(i).name);
        }
        return out.toString();
    }
}
