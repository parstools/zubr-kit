package parstools.zubr.lr;

import org.junit.jupiter.api.Test;
import parstools.zubr.grammar.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class LRkTest {
    private static Grammar book() { return TestGrammars.LR2_LL3_noSLL(); }

    @Test
    void bookGrammarNeedsTwoTokensIncludingEof() {
        Grammar grammar = book();
        assertFalse(new LR1(grammar).isConflictFree());
        assertFalse(new LRk(grammar, 1).isConflictFree());
        for (int k = 2; k <= 5; k++) {
            LRk parser = new LRk(grammar, k);
            assertTrue(parser.isConflictFree(), "k=" + k);
            for (List<String> word : List.of(List.of("c"), List.of("c", "a"),
                    List.of("b", "c", "a"), List.of("b", "c", "a", "a")))
                assertTrue(parser.accepts(word), word.toString());
            for (List<String> word : List.<List<String>>of(List.of(), List.of("b", "c"),
                    List.of("c", "a", "a"), List.of("b", "c", "a", "a", "a")))
                assertFalse(parser.accepts(word), word.toString());
        }

        LRk parser = new LRk(grammar, 2);
        int b = grammar.findT("b").getIndex(), c = grammar.findT("c").getIndex();
        int a = grammar.findT("a").getIndex();
        int afterB = parser.states().getFirst().transitions().get(grammar.findT("b"));
        int afterBC = parser.states().get(afterB).transitions().get(grammar.findT("c"));
        RowLR row = parser.row(afterBC);
        assertEquals(Set.of(Action.reduce(3)), row.actions(List.of(a, -1)));
        assertEquals(ActionKind.SHIFT, row.actions(List.of(a, a)).iterator().next().kind());
        assertTrue(row.actions(a).isEmpty(), "A prefix alone is not an ACTION key");
        assertTrue(row.actions(List.of(c, a)).isEmpty());
        assertNotNull(parser.row(0).actionRoot().next(b).next(c));
        assertTrue(row.actionRoot().next(a).actions().isEmpty());
        assertEquals(row.actions(List.of(a, -1)), row.actionRoot().next(a).next(-1).actions());
    }

    @Test
    void kOneMatchesExistingParsersAndTablesExactly() {
        for (Grammar grammar : List.of(book(), LRReferenceTablesTest.expressions(),
                LRReferenceTablesTest.assignment(), LRReferenceTablesTest.cc(),
                LRReferenceTablesTest.notLalr(), new Grammar(List.of("S -> A", "A -> a A", "A ->")))) {
            assertSameTables(new LR1(grammar), new LRk(grammar, 1));
            assertSameTables(new LALR(grammar), new LALRk(grammar, 1));
        }
    }

    private static void assertSameTables(AbstractLR expected, AbstractLR actual) {
        assertEquals(expected.stateCount(), actual.stateCount());
        for (int i = 0; i < expected.stateCount(); i++) {
            assertEquals(expected.states().get(i).core(), actual.states().get(i).core());
            assertEquals(expected.row(i).actions(), actual.row(i).actions());
            assertEquals(expected.row(i).gotos(), actual.row(i).gotos());
        }
    }

    @Test
    void allAllowedEofLengthsAndFullWordsOccur() {
        Grammar grammar = new Grammar(List.of("S -> A tail", "A -> token A", "A ->"));
        for (int k = 1; k <= 5; k++) {
            LRk parser = new LRk(grammar, k);
            assertTrue(parser.isConflictFree());
            Set<Integer> eofLengths = new HashSet<>();
            boolean fullWord = false;
            for (State state : parser.states())
                for (ItemLR0 item : state.items()) assertWord(((ItemLRk) item).lookahead(), k);
            for (int i = 0; i < parser.stateCount(); i++)
                for (List<Integer> word : parser.row(i).actions().keySet()) {
                    assertWord(word, k);
                    if (word.getLast() == -1) eofLengths.add(word.size());
                    else fullWord = true;
                }
            for (int n = 1; n <= k; n++) assertTrue(eofLengths.contains(n), "EOF length " + n);
            assertTrue(fullWord);
            assertTrue(parser.accepts(List.of("tail")));
            assertTrue(parser.accepts(List.of("token", "token", "tail")));
            assertFalse(parser.accepts(List.of()));
            assertFalse(parser.accepts(List.of("token")));
            assertFalse(parser.accepts(List.of("tail", "tail")));
        }
    }

    private static void assertWord(List<Integer> word, int k) {
        assertFalse(word.isEmpty());
        assertTrue(word.size() <= k);
        assertTrue(word.size() == k || word.getLast() == -1, word.toString());
        assertFalse(word.subList(0, word.size() - 1).contains(-1));
    }

    @Test
    void nullableAndLeftRecursiveGrammars() {
        for (int k = 1; k <= 4; k++) {
            for (Grammar grammar : List.of(new Grammar(List.of("S -> S token", "S ->")),
                    new Grammar(List.of("S -> A", "A -> token A", "A ->")))) {
                LRk parser = new LRk(grammar, k);
                assertTrue(parser.isConflictFree());
                assertTrue(parser.accepts(List.of()));
                assertTrue(parser.accepts(List.of("token", "token")));
                assertFalse(parser.accepts(List.of("unknown")));
            }
            LRk parser = new LRk(new Grammar(List.of("S -> A B C", "A -> a", "A ->",
                    "B -> b", "B ->", "C -> c", "C ->")), k);
            assertTrue(parser.isConflictFree());
            assertTrue(parser.accepts(List.of()));
            assertTrue(parser.accepts(List.of("a", "c")));
            assertTrue(parser.accepts(List.of("b", "c")));
            assertFalse(parser.accepts(List.of("c", "b")));
        }
    }

    @Test
    void genuinelyNeedsThreeTokens() {
        Grammar grammar = new Grammar(List.of("X -> Y", "X -> b Y a a", "Y -> c", "Y -> c a"));
        assertFalse(new LRk(grammar, 2).isConflictFree());
        LRk parser = new LRk(grammar, 3);
        assertTrue(parser.isConflictFree());
        assertTrue(parser.accepts(List.of("b", "c", "a", "a")));
        assertTrue(parser.accepts(List.of("b", "c", "a", "a", "a")));
        assertFalse(parser.accepts(List.of("b", "c", "a")));
    }

    @Test
    void mergingKStatesPreservesSourceAndReportsIntroducedConflicts() {
        for (int k = 1; k <= 3; k++) {
            Grammar grammar = LRReferenceTablesTest.cc();
            LRk canonical = new LRk(grammar, k);
            List<Map<List<Integer>, Set<Action>>> original = new ArrayList<>();
            for (int i = 0; i < canonical.stateCount(); i++) original.add(canonical.row(i).actions());
            LALRk merged = new LALRk(canonical);
            assertTrue(merged.stateCount() <= canonical.stateCount());
            assertTrue(merged.isConflictFree());
            assertTrue(merged.accepts(List.of("c", "d", "d")));
            assertFalse(merged.accepts(List.of("d")));
            for (int i = 0; i < original.size(); i++) assertEquals(original.get(i), canonical.row(i).actions());
            assertTrue(new LRk(LRReferenceTablesTest.notLalr(), k).isConflictFree());
            assertFalse(new LALRk(LRReferenceTablesTest.notLalr(), k).isConflictFree());
            assertTrue(new LALRk(LRReferenceTablesTest.notLalr(), k).conflicts()
                    .stream().allMatch(Conflict::reduceReduce));
        }
        // Merging the contexts after "c" and "b c" introduces shift/reduce on "a $".
        LALRk bookMerged = new LALRk(book(), 2);
        assertFalse(bookMerged.isConflictFree());
        assertTrue(bookMerged.conflicts().stream().anyMatch(Conflict::shiftReduce));
    }

    @Test
    void sparseTrieDoesNotExpandUnusedAlphabetCombinations() {
        List<String> rules = new ArrayList<>();
        // A large alphabet, but exactly one reachable sentence.
        rules.add("S -> first second third");
        for (int i = 0; i < 60; i++) rules.add("Unused -> t" + i);
        LRk parser = new LRk(new Grammar(rules), 6);
        int cells = 0, nodes = 0;
        for (int i = 0; i < parser.stateCount(); i++) {
            cells += parser.row(i).actionEntryCount();
            nodes += parser.row(i).actionNodeCount();
        }
        assertTrue(cells < 10, "Must not enumerate 63^6 columns");
        assertTrue(nodes < 30);
        assertTrue(parser.accepts(List.of("first", "second", "third")));
        assertFalse(parser.accepts(List.of("first", "second", "third", "t59")));
    }

    @Test
    void lookaheadTrieSharesPrefixesAndLookupStopsAtEof() {
        RowLR row = new RowLR();
        row.addAction(List.of(0, 1, 2), Action.reduce(1));
        row.addAction(List.of(0, 1, -1), Action.reduce(2));
        row.addAction(List.of(-1), Action.accept());
        assertEquals(6, row.actionNodeCount());
        assertEquals(3, row.actionEntryCount());
        assertEquals(Set.of(Action.reduce(2)), row.actionsAt(List.of(0, 1, -1, 99), 0, 3));
        assertEquals(Set.of(Action.accept()), row.actionsAt(List.of(-1), 0, 3));
        assertTrue(row.actionsAt(List.of(0, 1), 0, 3).isEmpty());
        assertTrue(row.actionsAt(List.of(0, 9, 2), 0, 3).isEmpty());
        assertThrows(UnsupportedOperationException.class, () -> row.actionRoot().nextTokens().clear());
        assertThrows(IllegalArgumentException.class, () -> row.addAction(List.of(-1, 0), Action.reduce(1)));
    }
}
