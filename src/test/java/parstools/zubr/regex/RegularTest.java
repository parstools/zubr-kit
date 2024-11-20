package parstools.zubr.regex;

import org.junit.jupiter.api.Test;
import parstools.zubr.lexer.EBNFLexer;
import parstools.zubr.regex.Concatenation;
import parstools.zubr.regex.Regular;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RegularTest {
    @Test
    void sinple() throws RuntimeException {
        String pattern =  "a*bb";
        Regular reg = new Regular(pattern, EBNFLexer.Mode.SIMPLE);
        assertEquals(pattern, reg.toString());
    }

    @Test
    void parsing() throws RuntimeException {
        String[] patterns =  {"(a|b)*abb", "(a|b)*ab+bc?", "ab|c","a|bc"};
        for (String pattern: patterns) {
            Regular reg = new Regular(pattern, EBNFLexer.Mode.SIMPLE);
            assertEquals(pattern, reg.toString());
        }
    }

    @Test
    void fakeGroup() throws RuntimeException {
        String[] patterns =  {"a(bcdef)g","a(b)c","ab(cd)a","ab()dc(c)a(bc)"};
        String[] expected = {"abcdefg","abc","abcda","abdccabc"};
        assertEquals(patterns.length, expected.length);
        for (int i = 0; i<patterns.length; i++) {
            Regular reg = new Regular(patterns[i], EBNFLexer.Mode.SIMPLE);
            int count = expected[i].length();
            assertEquals(expected[i], reg.toString());
            assertEquals(count, ((Concatenation)reg.getRoot()).getExpressions().size());
        }
    }

    @Test
    void fakeGroup2() throws RuntimeException {
        String[] patterns =  {"a(bcdef)g(a|b)c"};
        String[] expected = {"abcdefg(a|b)c"};
        assertEquals(patterns.length, expected.length);
        for (int i = 0; i<patterns.length; i++) {
            Regular reg = new Regular(patterns[i], EBNFLexer.Mode.SIMPLE);
            int count = 9;
            assertEquals(expected[i], reg.toString());
            assertEquals(count, ((Concatenation)reg.getRoot()).getExpressions().size());
        }
    }

    @Test
    void fakeGroup3() throws RuntimeException {
        String[] patterns =  {"((abcdef))"};
        String[] expected = {"abcdef"};
        assertEquals(patterns.length, expected.length);
        for (int i = 0; i<patterns.length; i++) {
            Regular reg = new Regular(patterns[i], EBNFLexer.Mode.SIMPLE);
            int count = 6;
            assertEquals(expected[i], reg.toString());
            assertEquals(count, ((Concatenation)reg.getRoot()).getExpressions().size());
        }
    }
}
