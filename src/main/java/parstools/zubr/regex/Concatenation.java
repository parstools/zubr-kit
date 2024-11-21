package parstools.zubr.regex;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Concatenation extends RegexExpression {
    final private List<RegexExpression> expressions;
    public Concatenation() {
        this.expressions = new ArrayList<>();
    }
    public void addExpression(RegexExpression expr) {
        expressions.add(expr);
    }
    public List<RegexExpression> getExpressions() {
        return expressions;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for (RegexExpression part: expressions) {
            String partStr = part.toString();
            if (part instanceof Alternation) {
                result.append("(");
                result.append(partStr);
                result.append(")");
            } else {
                if (!result.isEmpty())
                    result.append(" ");
                result.append(partStr);
            }
        }
        return result.toString();
    }

    public boolean needRaise() {
        if (expressions.isEmpty())
            return false;
        assert (expressions.size() > 1);
        for (RegexExpression part: expressions)
            if (part instanceof Concatenation)
                return true;
        return false;
    }

    public Concatenation raise() {
        Concatenation result = new  Concatenation();
        for (RegexExpression part: expressions)
            if (part instanceof Concatenation) {
                for (RegexExpression subpart : ((Concatenation) part).expressions)
                    result.addExpression(subpart);
            } else
                result.addExpression(part);
        return result;
    }

    @Override
    void addLiteralsToSet(Set<String> literalSet) {
        for (RegexExpression expression: expressions)
            expression.addLiteralsToSet(literalSet);
    }
}
