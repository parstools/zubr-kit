package parstools.zubr.lexer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EBNFLexerTest {
    @Test
    void normal() {
        EBNFLexer.Token[] expected = new EBNFLexer.Token[] {
                new EBNFLexer.Token(EBNFLexer.IDENT, "identifier"),
                new EBNFLexer.Token(EBNFLexer.STRING, "string literal"),
                new EBNFLexer.Token(EBNFLexer.COLON, ":"),
                new EBNFLexer.Token(EBNFLexer.PIPE, "|"),
                new EBNFLexer.Token(EBNFLexer.SEMICOLON, ";"),
                new EBNFLexer.Token(EBNFLexer.IDENT, "anotherIdent"),
                new EBNFLexer.Token(EBNFLexer.EOF, ""),
        };
        String input = "identifier 'string literal' : | ; // comment \n /* block comment */ anotherIdent";
        EBNFLexer lexer = new EBNFLexer(input, EBNFLexer.Mode.NORMAL);
        EBNFLexer.Token token;
        for (EBNFLexer.Token expectedToken: expected) {
            EBNFLexer.Token actualToken  = lexer.nextToken();
            assertEquals(expectedToken.type, actualToken.type);
            assertEquals(expectedToken.value, actualToken.value);
        }
    }

    @Test
    void simple() {
        EBNFLexer.Token[] expected = new EBNFLexer.Token[] {
                new EBNFLexer.Token(EBNFLexer.LPAREN, "("),
                new EBNFLexer.Token(EBNFLexer.IDENT, "a"),
                new EBNFLexer.Token(EBNFLexer.IDENT, "B"),
                new EBNFLexer.Token(EBNFLexer.RPAREN, ")"),
                new EBNFLexer.Token(EBNFLexer.STAR, "*"),
                new EBNFLexer.Token(EBNFLexer.IDENT, "v"),
                new EBNFLexer.Token(EBNFLexer.QUESTION, "?"),
                new EBNFLexer.Token(EBNFLexer.PIPE, "|"),
                new EBNFLexer.Token(EBNFLexer.LPAREN, "("),
                new EBNFLexer.Token(EBNFLexer.IDENT, "D"),
                new EBNFLexer.Token(EBNFLexer.IDENT, "f"),
                new EBNFLexer.Token(EBNFLexer.RPAREN, ")"),
                new EBNFLexer.Token(EBNFLexer.PLUS, "+"),
                new EBNFLexer.Token(EBNFLexer.IDENT, "a"),
                new EBNFLexer.Token(EBNFLexer.EOF, ""),
        };
        String input = "(aB)*v?|(Df)+a";
        EBNFLexer lexer = new EBNFLexer(input, EBNFLexer.Mode.SIMPLE);
        EBNFLexer.Token token;
        for (EBNFLexer.Token expectedToken: expected) {
            EBNFLexer.Token actualToken  = lexer.nextToken();
            assertEquals(expectedToken.type, actualToken.type);
            assertEquals(expectedToken.value, actualToken.value);
        }
    }
}
