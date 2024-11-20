package parstools.zubr.ebnf;

import org.junit.jupiter.api.Test;
import parstools.zubr.grammar.Grammar;
import parstools.zubr.grammar.names.LetterNameGenerator;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EBNFTest {

    String[] fromSecondElement(String[] originalArray) {
        String[] newArray = new String[originalArray.length - 1];
        System.arraycopy(originalArray, 1, newArray, 0, originalArray.length - 1);
        return newArray;
    }

    @Test
    void convertToLR() throws RuntimeException {
        //loops changes to left recursion, suitable to LR parsers
        String[][] testCases = {
                {"A:a?", "A -> a", "A -> ε"},
                {"A:a+",  "A -> A a", "A -> a"},
                {"A:a*",  "A -> A a", "A -> ε"},
                {"A:ab+c", "A -> a B c","B -> B b","B -> b"},
                {"A:a(Bc)*d?", "A -> a C D","C -> C B c","C -> ε","D -> d","D -> ε"}
        };
        for (String[] testCase : testCases) {
            String[] input = { testCase[0] };
            EBNFGrammar egrammar = new EBNFGrammar(input);
            EBNFtoBNFConverter ebnfConv = new EBNFtoBNFConverter(false, new LetterNameGenerator());
            Grammar grammar = ebnfConv.convert(egrammar);
            String[] expected = fromSecondElement(testCase);
            String[] actual = grammar.toArray();
            assertEquals(expected.length, actual.length);
            for (int i= 0; i<expected.length; i++)
                assertEquals(expected[i], actual[i]);
        }
    }

    @Test
    void convertToLL() throws RuntimeException {
        //loops changes to right recursion, suitable to LL parsers
        String[][] testCases = {
                {"A:a?", "A -> a", "A -> ε"},
                {"A:a+", "A -> a A", "A -> a"},
                {"A:a*", "A -> a A", "A -> ε"},
                {"A:ab+c", "A -> a B c","B -> b B","B -> b"},
                {"A:a(Bc)*d?", "A -> a C D","C -> B c C","C -> ε","D -> d","D -> ε"}
        };
        for (String[] testCase : testCases) {
            String[] input = { testCase[0] };
            EBNFGrammar egrammar = new EBNFGrammar(input);
            EBNFtoBNFConverter ebnfConv = new EBNFtoBNFConverter(true, new LetterNameGenerator());
            Grammar grammar = ebnfConv.convert(egrammar);
            String[] expected = fromSecondElement(testCase);
            String[] actual = grammar.toArray();
            assertEquals(expected.length, actual.length);
            for (int i= 0; i<expected.length; i++)
                assertEquals(expected[i], actual[i]);
        }
    }

    @Test
    void simpleQuantifier() throws RuntimeException {
        String[] input = {
                "A:a+",
        };
        String[] expected = {
                "A -> A a",
                "A -> a",
        };
        EBNFGrammar egrammar = new EBNFGrammar(input);
        EBNFtoBNFConverter ebnfConv = new EBNFtoBNFConverter(false, new LetterNameGenerator());
        Grammar grammar = ebnfConv.convert(egrammar);
        String[] actual = grammar.toArray();
        assertEquals(expected.length, actual.length);
        for (int i= 0; i<expected.length; i++)
            assertEquals(expected[i], actual[i]);
    }

    @Test
    void simpleAlt() throws RuntimeException {
        String[] input = {
                "A:aB|Cd|e|",
        };
        String[] expected = {
                "A -> a B",
                "A -> C d",
                "A -> e",
                "A -> ε",
        };
        EBNFGrammar egrammar = new EBNFGrammar(input);
        EBNFtoBNFConverter ebnfConv = new EBNFtoBNFConverter(false, new LetterNameGenerator());
        Grammar grammar = ebnfConv.convert(egrammar);
        String[] actual = grammar.toArray();
        assertEquals(expected.length, actual.length);
        for (int i= 0; i<expected.length; i++)
            assertEquals(expected[i], actual[i]);
    }

    @Test
    void noChange() throws RuntimeException {
        String[] input = {
                "A:aBCde",
        };
        String[] expected = {
                "A -> a B C d e",
        };
        EBNFGrammar egrammar = new EBNFGrammar(input);
        EBNFtoBNFConverter ebnfConv = new EBNFtoBNFConverter(false, new LetterNameGenerator());
        Grammar grammar = ebnfConv.convert(egrammar);
        String[] actual = grammar.toArray();
        assertEquals(expected.length, actual.length);
        for (int i= 0; i<expected.length; i++)
            assertEquals(expected[i], actual[i]);
    }

    @Test
    void ConcatZERO_OR_ONE() throws RuntimeException {
        String[] input = {
                "A:AB?c",
                "B:ac"
        };
        List<String> expected = Arrays.asList(
                "A -> A C c",
                "B -> a c",
                "C -> B",
                "C -> ε"
        );
        EBNFGrammar egrammar = new EBNFGrammar(input);
        EBNFtoBNFConverter ebnfConv = new EBNFtoBNFConverter(false, new LetterNameGenerator());
        Grammar grammar = ebnfConv.convert(egrammar);
        String[] actual = grammar.toArray();
        assertEquals(expected.size(), actual.length);
        for (int i= 0; i<expected.size(); i++)
            assertEquals(expected.get(i), actual[i]);
    }

    @Test
    void ConcatONE_OR_MORE_left() {
        String[] input = {
                "A:AB+c",
        };
        String[] expected = {
                "A -> A C c",
                "C -> C B",
                "C -> B"
        };
        EBNFGrammar egrammar = new EBNFGrammar(input);
        EBNFtoBNFConverter ebnfConv = new EBNFtoBNFConverter(false, new LetterNameGenerator());
        Grammar grammar = ebnfConv.convert(egrammar);
        String[] actual = grammar.toArray();
        assertEquals(expected.length, actual.length);
        for (int i = 0; i < expected.length; i++)
            assertEquals(expected[i], actual[i]);
    }

    void ConcatMulti() throws RuntimeException {
        String[] input = {
                "A:A*B?c+d",
        };
        String[] expected = {
                "A->CBDd",
                "A->CDd",
                "C->CA",
                "C->",
                "D->Dc",
                "D->c"
        };
        EBNFGrammar egrammar = new EBNFGrammar(input);
        EBNFtoBNFConverter ebnfConv = new EBNFtoBNFConverter(false, new LetterNameGenerator());
        Grammar grammar = ebnfConv.convert(egrammar);
        String[] actual = grammar.toArray();
        assertEquals(expected.length, actual.length);
        for (int i= 0; i<expected.length; i++)
            assertEquals(expected[i], actual[i]);
    }

    void ConcatMultiTwoQuest() throws RuntimeException {
        String[] input = {
                "A:A*B?c+d?",
        };
        String[] expected = {
                "A->A*B?c+d?",
        };
        EBNFGrammar egrammar = new EBNFGrammar(input);
        EBNFtoBNFConverter ebnfConv = new EBNFtoBNFConverter(false, new LetterNameGenerator());
        Grammar grammar = ebnfConv.convert(egrammar);
        String[] actual = grammar.toArray();
        assertEquals(expected.length, actual.length);
        for (int i= 0; i<expected.length; i++)
            assertEquals(expected[i], actual[i]);
    }

    void ConcatONE_OR_MORE_right() {
        String[] input = {
                "A:AB+c",
        };
        String[] expected = {
                "A->ACc",
                "C->BC",
                "C->B"
        };
        EBNFGrammar egrammar = new EBNFGrammar(input);
        EBNFtoBNFConverter ebnfConv = new EBNFtoBNFConverter(false, new LetterNameGenerator());
        Grammar grammar = ebnfConv.convert(egrammar);
        String[] actual = grammar.toArray();
        assertEquals(expected.length, actual.length);
        for (int i = 0; i < expected.length; i++)
            assertEquals(expected[i], actual[i]);
    }

    @Test
    void twoLines() throws RuntimeException {
        String[] input = {
                "A:Ba*",
                "A:b+",
        };
        String[] expected = {
                "A -> B C",
                "A -> b A",
                "A -> b",
                "C -> a C",
                "C -> ε",
        };
        EBNFGrammar egrammar = new EBNFGrammar(input);
        EBNFtoBNFConverter ebnfConv = new EBNFtoBNFConverter(true, new LetterNameGenerator());
        Grammar grammar = ebnfConv.convert(egrammar);
        String[] actual = grammar.toArray();
        assertEquals(expected.length, actual.length);
        for (int i= 0; i<expected.length; i++)
            assertEquals(expected[i], actual[i]);
    }
}
