package parstools.zubr.lexer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EBNFLexerTest {
    @Test
    void normal() {
        String input = "identifier 'string literal' : | ; // comment \n /* block comment */ anotherIdent";
        EBNFLexer.Token[] expected = new EBNFLexer.Token[] {
                new EBNFLexer.Token(EBNFLexer.IDENT, "identifier", 0),
                new EBNFLexer.Token(EBNFLexer.STRING, "string literal", 12),
                new EBNFLexer.Token(EBNFLexer.COLON, ":", 28),
                new EBNFLexer.Token(EBNFLexer.PIPE, "|",30),
                new EBNFLexer.Token(EBNFLexer.SEMICOLON, ";", 32),
                new EBNFLexer.Token(EBNFLexer.IDENT, "anotherIdent", 67),
                new EBNFLexer.Token(EBNFLexer.EOF_TOKEN, "", input.length()),
        };
        EBNFLexer lexer = new EBNFLexer(input, EBNFLexer.Mode.NORMAL);
        EBNFLexer.Token token;
        for (EBNFLexer.Token expectedToken: expected) {
            EBNFLexer.Token actualToken  = lexer.nextToken();
            assertEquals(expectedToken.type, actualToken.type);
            assertEquals(expectedToken.value, actualToken.value);
            assertEquals(expectedToken.index, actualToken.index);
        }
    }

    @Test
    void simple() {
        String input = "(aB)*v?|(Df)+a";
        EBNFLexer.Token[] expected = new EBNFLexer.Token[] {
                new EBNFLexer.Token(EBNFLexer.LPAREN, "(", 0),
                new EBNFLexer.Token(EBNFLexer.IDENT, "a",1),
                new EBNFLexer.Token(EBNFLexer.IDENT, "B", 2),
                new EBNFLexer.Token(EBNFLexer.RPAREN, ")", 3),
                new EBNFLexer.Token(EBNFLexer.STAR, "*",4),
                new EBNFLexer.Token(EBNFLexer.IDENT, "v", 5),
                new EBNFLexer.Token(EBNFLexer.QUESTION, "?", 6),
                new EBNFLexer.Token(EBNFLexer.PIPE, "|", 7),
                new EBNFLexer.Token(EBNFLexer.LPAREN, "(", 8),
                new EBNFLexer.Token(EBNFLexer.IDENT, "D" ,9),
                new EBNFLexer.Token(EBNFLexer.IDENT, "f", 10),
                new EBNFLexer.Token(EBNFLexer.RPAREN, ")", 11),
                new EBNFLexer.Token(EBNFLexer.PLUS, "+", 12),
                new EBNFLexer.Token(EBNFLexer.IDENT, "a",13),
                new EBNFLexer.Token(EBNFLexer.EOF_TOKEN, "", input.length()),
        };
        EBNFLexer lexer = new EBNFLexer(input, EBNFLexer.Mode.SIMPLE);
        EBNFLexer.Token token;
        for (EBNFLexer.Token expectedToken: expected) {
            EBNFLexer.Token actualToken  = lexer.nextToken();
            assertEquals(expectedToken.type, actualToken.type);
            assertEquals(expectedToken.value, actualToken.value);
        }
    }
}
