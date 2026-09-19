package parstools.zubr;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class MainTest {
    @Test
    void removesOldGrammarLabelsFromEveryComment() {
        assertEquals(";description tail", Main.cleanGrammarLabels(
                ";[LR(1)] description [notLR(9)] [SLR] [LL(2)] [notLL(9)] tail"));
        assertEquals(";description", Main.cleanGrammarLabels(";[LALR(2)] description"));
        assertEquals(";description", Main.cleanGrammarLabels(";[notLALR(2)] description"));
        assertEquals(";description", Main.cleanGrammarLabels(";[ambig] description"));
        assertEquals(";description", Main.cleanGrammarLabels(";[LRabcd] description"));
    }

    @Test
    void addsLrAndLlLabelsToTheFirstCommentAndPreservesTheBlock() {
        List<String> input = List.of(
                ";old [LR(9)] description",
                ";second [notLL(9)] comment",
                "S -> a");

        List<String> result = Main.labelGrammar(input);

        assertEquals(";[LR(0)] [LALR(1)] [LL(1)] old description", result.get(0));
        assertEquals(";second comment", result.get(1));
        assertEquals("S -> a", result.get(2));
    }

    @Test
    void addsACommentWhenTheGrammarHasNone() {
        assertEquals(List.of(";[LR(0)] [LALR(1)] [LL(1)]", "S -> token"),
                Main.labelGrammar(List.of("S -> token")));
    }

    @Test
    void labelsTheBookExampleAsLrTwo() {
        List<String> result = Main.labelGrammar(List.of(
                ";book page 148",
                "X -> Y",
                "X -> b Y a",
                "Y -> c",
                "Y -> c a"));

        assertEquals(";[LR(2)] [notLALR(2)] [notLL(6)] book page 148", result.getFirst());
    }

    @Test
    void labelsAnLrOneGrammarWithLalrConflict() {
        List<String> result = Main.labelGrammar(List.of(
                ";canonical LR(1), but not LALR(1)",
                "S -> a A d",
                "S -> a B e",
                "S -> b B d",
                "S -> b A e",
                "A -> c",
                "B -> c"));

        assertEquals(";[LR(1)] [notLALR(1)] [LL(2)] canonical LR(1), but not LALR(1)",
                result.getFirst());
    }

    @Test
    void ambiguityIsTheOnlyLabel() {
        List<String> result = Main.labelGrammar(List.of(
                ";[LR(1)] [LL(1)] ambiguous expression grammar",
                "E -> E + E",
                "E -> i"));

        assertEquals(";[ambig] ambiguous expression grammar", result.getFirst());
        assertFalse(result.getFirst().contains("[LR"));
        assertFalse(result.getFirst().contains("[LL"));
    }
}
