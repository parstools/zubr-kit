package parstools.zubr.regex;

import java.util.Set;

public class Literal extends RegexExpression {
    final private String value;
    public Literal(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    void addLiteralsToSet(Set<String> literalSet) {
        literalSet.add(value);
    }
}