package parstools.zubr.ebnf;

import parstools.zubr.lexer.EBNFLexer;
import parstools.zubr.regex.Regular;

public class EBNFRule {
    String nonTerminal;
    Regular production;
    public EBNFRule(String stringForm) {
        String[] parts = stringForm.split(":");
        if (parts.length != 2) {
            throw new RuntimeException("Invalid rule (missing '->'): " + stringForm);
        }
        nonTerminal = parts[0].trim();
        String rhs = parts[1].trim();
        production = new Regular(rhs, EBNFLexer.Mode.SIMPLE);
    }
}
