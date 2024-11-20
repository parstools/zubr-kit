package parstools.zubr.lexer;

import org.junit.jupiter.api.Test;
import parstools.zubr.input.EBNFTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EBNFLexerTest {
    @Test
    void test() {
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
        EBNFLexer lexer = new EBNFLexer(input);
        EBNFLexer.Token token;
        for (EBNFLexer.Token expectedToken: expected) {
            EBNFLexer.Token actualToken  = lexer.nextToken();
            assertEquals(expectedToken.type, actualToken.type);
            assertEquals(expectedToken.value, actualToken.value);
        }
    }
}
