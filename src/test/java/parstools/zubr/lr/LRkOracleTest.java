package parstools.zubr.lr;

import org.junit.jupiter.api.Test;
import parstools.zubr.grammar.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class LRkOracleTest {
    @Test
    void sparseConstructionEqualsIndependentDenseOracle() {
        List<Grammar> grammars = List.of(
                TestGrammars.LR2_LL3_noSLL(),
                new Grammar(List.of("X -> Y", "X -> b Y a a", "Y -> c", "Y -> c a")),
                LRReferenceTablesTest.cc(),
                LRReferenceTablesTest.notLalr(),
                new Grammar(List.of("S -> A B", "A -> a A", "A ->", "B -> b B", "B ->")),
                new Grammar(List.of("S -> S a", "S ->")),
                new Grammar(List.of("S -> A", "A ->")),
                new Grammar(List.of("S -> A", "A -> B", "B -> A", "B ->")),
                new Grammar(List.of("S -> a", "S -> a")));
        for (Grammar grammar : grammars) {
            for (int k = 1; k <= 3; k++) {
                LRk sparse = new LRk(grammar, k);
                NaiveLRk dense = new NaiveLRk(grammar, k);
                assertEquals(dense.states.size(), sparse.stateCount(), "State count, k=" + k);
                for (int state = 0; state < sparse.stateCount(); state++) {
                    Set<NaiveLRk.Item> actual = new HashSet<>();
                    for (ItemLR0 item : sparse.states().get(state).items())
                        actual.add(new NaiveLRk.Item(sparse.ruleNumber(item.rule), item.dotPosition,
                                ((ItemLRk) item).lookahead()));
                    assertEquals(dense.states.get(state), actual, "Items, k=" + k + ", state=" + state);
                    assertEquals(dense.transitions.get(state), sparse.states().get(state).transitions());
                    int populated = 0;
                    for (List<Integer> column : dense.columns) {
                        Set<Action> expected = dense.tables.get(state).get(column);
                        if (!expected.isEmpty()) populated++;
                        assertEquals(expected, sparse.row(state).actions(column),
                                "ACTION, k=" + k + ", state=" + state + ", column=" + column);
                    }
                    assertEquals(populated, sparse.row(state).actionEntryCount());
                }
                assertEquals(dense.isConflictFree(), sparse.isConflictFree());
            }
        }
    }

    @Test
    void exhaustiveShortInputsAgreeWithOracleAndBookLanguage() {
        Grammar grammar = TestGrammars.LR2_LL3_noSLL();
        Set<List<String>> language = Set.of(List.of("c"), List.of("c", "a"),
                List.of("b", "c", "a"), List.of("b", "c", "a", "a"));
        List<List<String>> words = words(List.of("a", "b", "c"), 6);
        for (int k = 2; k <= 4; k++) {
            LRk sparse = new LRk(grammar, k);
            NaiveLRk dense = new NaiveLRk(grammar, k);
            for (List<String> word : words) {
                assertEquals(language.contains(word), dense.accepts(word), "Oracle: " + word);
                assertEquals(language.contains(word), sparse.accepts(word), "Trie: " + word);
            }
        }
    }

    @Test
    void recursiveLanguageAndMergedParserAgreeOnAllShortInputs() {
        Grammar grammar = new Grammar(List.of("S -> A B", "A -> a A", "A ->", "B -> b B", "B ->"));
        for (int k = 1; k <= 3; k++) {
            NaiveLRk dense = new NaiveLRk(grammar, k);
            LRk sparse = new LRk(grammar, k);
            LALRk merged = new LALRk(sparse);
            assertTrue(merged.isConflictFree());
            for (List<String> word : words(List.of("a", "b"), 6)) {
                boolean expected = !String.join("", word).contains("ba");
                assertEquals(expected, sparse.accepts(word), word.toString());
                assertEquals(expected, dense.accepts(word), word.toString());
                assertEquals(expected, merged.accepts(word), word.toString());
            }
        }
    }

    private static List<List<String>> words(List<String> alphabet, int maxLength) {
        List<List<String>> result = new ArrayList<>();
        List<List<String>> layer = List.of(List.of());
        for (int length = 0; length <= maxLength; length++) {
            result.addAll(layer);
            List<List<String>> next = new ArrayList<>();
            for (List<String> prefix : layer)
                for (String token : alphabet) {
                    List<String> word = new ArrayList<>(prefix);
                    word.add(token);
                    next.add(List.copyOf(word));
                }
            layer = next;
        }
        return result;
    }
}
