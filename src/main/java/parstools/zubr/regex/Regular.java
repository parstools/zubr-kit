package parstools.zubr.regex;

import parstools.zubr.lexer.EBNFLexer;

import java.util.HashSet;
import java.util.Set;

public class Regular {
    final private RegexExpression root;
    private Set<String> literalSet = new HashSet<>();

    public Set<String> literals() {
        return literalSet;
    }

    public Regular(String pattern, EBNFLexer.Mode mode) throws RuntimeException {
        Parser parser = new Parser();
        this.root = parser.parse(pattern, mode);
        root.addLiteralsToSet(literalSet);
    }

    public RegexExpression getRoot() {
        return root;
    }

    @Override
    public String toString() {
        return root.toString();
    }
}