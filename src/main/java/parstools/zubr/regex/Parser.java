package parstools.zubr.regex;

import parstools.zubr.lexer.EBNFLexer;

class Parser {
    private EBNFLexer lexer;

    public Parser(String pattern) {
        lexer = new EBNFLexer(pattern, EBNFLexer.Mode.SIMPLE);
    }

    public RegexExpression parse() throws RuntimeException {
        RegexExpression expr = parseExpression();
        EBNFLexer.Token token = lexer.peek();
        if (token.type != EBNFLexer.EOF)
            throw new RuntimeException("Unexpected symbol in position " + token.index);
        return expr;
    }

    private RegexExpression parseExpression() throws RuntimeException {
        RegexExpression term = parseTerm();
        if (lexer.peek().type == EBNFLexer.PIPE) {
            Alternation alt = new Alternation();
            alt.addAlternative(term);
            while (lexer.peek().type != EBNFLexer.EOF && lexer.peek().type == EBNFLexer.PIPE) {
                lexer.consume(); // consumes '|'
                RegexExpression nextTerm = parseTerm();
                alt.addAlternative(nextTerm);
            }
            return alt;
        } else {
            return term;
        }
    }

    private RegexExpression parseTerm() throws RuntimeException {
        Concatenation concat = new Concatenation();
        EBNFLexer.Token token = lexer.peek();
        int type = token.type;
        while (type != EBNFLexer.EOF && type != EBNFLexer.RPAREN && type != EBNFLexer.PIPE) {
            RegexExpression factor = parseFactor();
            concat.addExpression(factor);
            token = lexer.peek();
            type = token.type;
        }
        if (concat.getExpressions().size() == 1) {
            return concat.getExpressions().getFirst();
        } else
            if (concat.needRaise())
                return concat.raise();
            else
                return concat;
    }

    private RegexExpression parseFactor() throws RuntimeException {
        RegexExpression base = parseBase();
        EBNFLexer.Token token = lexer.peek();
        int type = token.type;
        if (type != EBNFLexer.EOF) {
            if (type == EBNFLexer.STAR || type == EBNFLexer.PLUS || type == EBNFLexer.QUESTION) {
                lexer.consume();
                Quantifier quant = switch (type) {
                    case EBNFLexer.STAR -> Quantifier.ZERO_OR_MORE;
                    case EBNFLexer.PLUS -> Quantifier.ONE_OR_MORE;
                    case EBNFLexer.QUESTION -> Quantifier.ZERO_OR_ONE;
                    default -> throw new RuntimeException("Unknown quantifier: " + token.value);
                };
                return new QuantifierExpression(base, quant);
            }
        }
        return base;
    }

    private RegexExpression parseBase() throws RuntimeException {
        EBNFLexer.Token token = lexer.peek();
        int type = token.type;
        if (type == EBNFLexer.LPAREN) {
            lexer.consume();
            RegexExpression expr = parseExpression();
            token = lexer.peek();
            if (token.type != EBNFLexer.RPAREN) {
                throw new RuntimeException("Expected ')' in position " + token.index);
            }
            lexer.consume();
            return expr;
        } else {
            lexer.consume();
            return new Literal(token.value);
        }
    }
}