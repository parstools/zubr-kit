package ll;

import grammar.Grammar;
import grammar.TestGrammars;
import org.junit.jupiter.api.Test;

public class ParsingTableTest {
    @Test
    void testLL1() {
        Grammar g = TestGrammars.stdLL1();
        ParsingTable table = new ParsingTable(g);
        table.createLL1();
    }

    @Test
    void testLL_1() {
        Grammar g = TestGrammars.LL1();
        ParsingTable table = new ParsingTable(g);
        table.createLL(1);
    }

    @Test
    void testLL_2() {
        Grammar g = TestGrammars.LL2();
        ParsingTable table = new ParsingTable(g);
        table.createLL(2);
    }
}