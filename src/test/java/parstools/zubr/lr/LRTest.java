package parstools.zubr.lr;

import org.junit.jupiter.api.Test;
import parstools.zubr.grammar.Grammar;
import parstools.zubr.grammar.TestGrammars;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LRTest {
    @Test
    void createStatesLR0() {
        Grammar g = TestGrammars.LRwikiLR0();
        StatesLR0 states = new StatesLR0(g);
        AbstractLR lr = new AbstractLR();
        states.createStates(lr);
        assertEquals(9, states.size());
    }

    @Test
    void LR0() {
        Grammar g = TestGrammars.LRwikiLR0();
        LR0 parser = new LR0(g);
        parser.row(0).toString();
    }

    @Test
    void SLR() {
        Grammar g = TestGrammars.LRwikiLR0();
        SLR parser = new SLR(g);
        parser.row(0).toString();
    }

    void LR1() {
        Grammar g = TestGrammars.LRwikiLR1();
        LR1 parser = new LR1(g);
        parser.row(0).toString();
    }

    void LALR() {
        Grammar g = TestGrammars.LRwikiLR1();
        LALR parser = new LALR(g);
        parser.row(0).toString();
    }
}
