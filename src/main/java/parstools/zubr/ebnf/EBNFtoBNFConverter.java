package parstools.zubr.ebnf;

import parstools.zubr.grammar.Grammar;
import parstools.zubr.grammar.Nonterminal;
import parstools.zubr.grammar.Rule;
import parstools.zubr.grammar.names.NameGenerator;
import parstools.zubr.lex.regex.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Converter class that transforms EBNF rules into BNF.
 */
public class EBNFtoBNFConverter {
    private Grammar grammar;
    private EBNFGrammar egrammar;
    boolean preferRightRecursion;
    private NameGenerator generator;

    public EBNFtoBNFConverter(boolean preferRightRecursion, NameGenerator generator) {
        this.preferRightRecursion = preferRightRecursion;
        this.generator = generator;
    }

    public Grammar convert(EBNFGrammar egrammar) throws RuntimeException {
        this.egrammar = egrammar;
        List<EBNFRule> ebnfRules = egrammar.rules;
        grammar = new Grammar();
        for (EBNFRule rule : ebnfRules) {
            generator.registerName(rule.nonTerminal);
            Set<String> symNames = rule.production.literals();
            for (String symName: symNames)
                generator.registerName(symName);
            grammar.nonterminals.add(new Nonterminal(grammar, rule.nonTerminal));
        }

        for (EBNFRule rule : ebnfRules) {
            RegexExpression expr = rule.production.getRoot();
            List<String> rhsSymbols = processExpression(expr, rule.nonTerminal);
            grammar.addRule(new Rule(grammar, rule.nonTerminal, rhsSymbols));
        }
        return grammar;
    }

    /**
     * Recursively process a RegexExpression and generate corresponding BNF symbols.
     *
     * @param expr    The regex expression to process
     * @param parent  The parent non-terminal (for generating unique names)
     * @return List of Symbols representing the processed expression
     */
    private List<String> processExpression(RegexExpression expr, String parent) {
        List<String> symbols = new ArrayList<>();
        if (expr instanceof Concatenation) {
            processConcatenation((Concatenation) expr, parent, symbols);
        } else if (expr instanceof Alternation) {
            processAlternation((Alternation) expr, parent, symbols);
        } else if (expr instanceof QuantifierExpression) {
            processQuantifierExpression((QuantifierExpression) expr, parent, symbols);
        } else if (expr instanceof Literal) {
            Literal lit = (Literal) expr;
            symbols.add(""+lit.getValue());//todo value ma być stringiem
        } else {
            throw new RuntimeException("Unsupported RegexExpression: " + expr.getClass().getName());
        }
        return symbols;
    }

    private void processQuantifierExpression(QuantifierExpression expr, String parent, List<String> symbols) {
        QuantifierExpression qexpr = expr;
        Quantifier quant = qexpr.getQuantifier();
        RegexExpression subExpr = qexpr.getExpression();
        List<String> subSymbols = processExpression(subExpr, parent);
        // Depending on the quantifier, create new productions
        String newNT = generator.generate(parent);
        switch (quant) {
            case ZERO_OR_MORE: // *
                // newNT -> subSymbols newNT | ε
                List<String> p1 = new ArrayList<>();
                if (preferRightRecursion) {
                    p1.addAll(subSymbols);
                    p1.add(newNT);
                } else {
                    p1.add(newNT);
                    p1.addAll(subSymbols);
                }
                grammar.addRule(new Rule(grammar, newNT, p1));
                grammar.addRule(new Rule(grammar, newNT, null));// Empty production = ε
                break;
            case ONE_OR_MORE: // +
                // newNT -> subSymbols newNT | subSymbols
                List<String> p2 = new ArrayList<>();
                if (preferRightRecursion) {
                    p2.addAll(subSymbols);
                    p2.add(newNT);
                } else {
                    p2.add(newNT);
                    p2.addAll(subSymbols);
                }
                grammar.addRule(new Rule(grammar, newNT, p2));
                grammar.addRule(new Rule(grammar, newNT, subSymbols));
                break;
            case ZERO_OR_ONE: // ?
                // newNT -> subSymbols | ε
                grammar.addRule(new Rule(grammar, newNT, subSymbols));
                grammar.addRule(new Rule(grammar, newNT, null));// Empty production = ε
                break;
        }
        symbols.add(newNT);
    }

    private void processAlternation(Alternation expr, String parent, List<String> symbols) {
        // Introduce a new non-terminal for alternation
        String newNT = generator.generate(parent);
        Alternation alt = expr;
        for (RegexExpression alternative : alt.getAlternatives()) {
            // Process each alternative expression
            List<String> altSymbols = processExpression(alternative, parent);
            grammar.addRule(new Rule(grammar, newNT, altSymbols));
        }
        symbols.add(newNT);
    }

    private void processConcatenation(Concatenation expr, String parent, List<String> symbols) {
        Concatenation concat = expr;
        for (RegexExpression subExpr : concat.getExpressions()) {
            symbols.addAll(processExpression(subExpr, parent));
        }
    }
}