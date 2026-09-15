package parstools.zubr.lr;

import org.junit.jupiter.api.Test;
import parstools.zubr.grammar.*;
import parstools.zubr.set.Sequence;
import java.time.Duration;
import java.util.*;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

class LRAlgorithmsTest {
    private static final List<Function<Grammar, AbstractLR>> PARSERS =
            List.of(LR0::new, SLR::new, LALR::new, LR1::new);

    @Test
    void classBoundariesAndConflictKinds() {
        LR0 lr0 = new LR0(LRReferenceTablesTest.expressions());
        assertEquals(2, lr0.conflicts().size());
        assertTrue(lr0.conflicts().stream().allMatch(Conflict::shiftReduce));
        assertTrue(new SLR(LRReferenceTablesTest.expressions()).isConflictFree());
        SLR slr = new SLR(LRReferenceTablesTest.assignment());
        assertEquals(1, slr.conflicts().size());
        assertTrue(slr.conflicts().getFirst().shiftReduce());
        assertTrue(new LALR(LRReferenceTablesTest.assignment()).isConflictFree());
        assertTrue(new LR1(LRReferenceTablesTest.notLalr()).isConflictFree());
        LALR lalr = new LALR(LRReferenceTablesTest.notLalr());
        assertEquals(2, lalr.conflicts().size());
        assertTrue(lalr.conflicts().stream().allMatch(Conflict::reduceReduce));
        for (var factory : PARSERS) {
            AbstractLR ambiguous = factory.apply(new Grammar(List.of("E -> E + E", "E -> id")));
            assertFalse(ambiguous.isConflictFree());
            assertTrue(ambiguous.conflicts().stream().anyMatch(Conflict::shiftReduce));
            assertThrows(IllegalStateException.class, () -> ambiguous.accepts(List.of("id")));
        }
    }

    @Test
    void recognitionHandlesTokenNamesAndEndOfInput() {
        for (var factory : List.<Function<Grammar, AbstractLR>>of(SLR::new, LALR::new, LR1::new)) {
            AbstractLR parser = factory.apply(LRReferenceTablesTest.expressions());
            assertTrue(parser.accepts(List.of("id")));
            assertTrue(parser.accepts(List.of("id", "+", "id", "*", "id")));
            assertTrue(parser.accepts(List.of("(", "id", "+", "id", ")", "*", "id")));
            assertFalse(parser.accepts(List.of()));
            assertFalse(parser.accepts(List.of("id", "id")));
            assertFalse(parser.accepts(List.of("(", "id")));
            assertFalse(parser.accepts(List.of("id", "+")));
            assertFalse(parser.accepts(List.of("unknown")));
            assertFalse(parser.accepts(List.of("id", "$")));
        }
        for (var factory : PARSERS) {
            AbstractLR parser = factory.apply(LRReferenceTablesTest.simple());
            assertTrue(parser.accepts(List.of("id", "+", "id", "+", "id")));
            assertFalse(parser.accepts(List.of("+", "id")));
        }
    }

    @Test
    void nullableStartAcceptsEmptyInputInAllVariants() {
        for (var factory : PARSERS) {
            AbstractLR parser = factory.apply(new Grammar(List.of("S -> A", "A ->")));
            assertTrue(parser.isConflictFree());
            assertTrue(parser.accepts(List.of()));
            assertFalse(parser.accepts(List.of("a")));
            assertEquals(Set.of(Action.reduce(2)), parser.row(0).actions(-1));
        }
    }

    @Test
    void nullableSuffixPropagatesFirstAndInheritedLookahead() {
        Grammar grammar = new Grammar(List.of("S -> A B C", "A -> a", "A ->",
                "B -> b", "B ->", "C -> c", "C ->"));
        for (var factory : List.<Function<Grammar, AbstractLR>>of(SLR::new, LR1::new, LALR::new)) {
            AbstractLR parser = factory.apply(grammar);
            assertTrue(parser.isConflictFree());
            for (List<String> word : List.<List<String>>of(List.of(), List.of("a"), List.of("b"),
                    List.of("c"), List.of("a", "c"), List.of("b", "c"), List.of("a", "b", "c")))
                assertTrue(parser.accepts(word), word.toString());
            assertFalse(parser.accepts(List.of("c", "a")));
        }
        LR1 parser = new LR1(grammar);
        Set<Integer> lookaheads = new HashSet<>();
        for (ItemLR0 item : parser.states().getFirst().items())
            if (item.rule.owner == grammar.findNt("A")) lookaheads.add(((ItemLR1) item).terminal());
        assertEquals(Set.of(-1, grammar.findT("b").getIndex(), grammar.findT("c").getIndex()), lookaheads);
    }

    @Test
    void fixedPointHandlesNullableCyclesAndLeftRecursion() {
        assertTimeoutPreemptively(Duration.ofSeconds(5), () -> {
            Grammar cyclic = new Grammar(List.of("S -> A", "A -> B", "B -> A", "B ->"));
            for (var factory : PARSERS)
                assertFalse(factory.apply(cyclic).isConflictFree());
            for (var factory : List.<Function<Grammar, AbstractLR>>of(SLR::new, LR1::new, LALR::new)) {
                AbstractLR parser = factory.apply(new Grammar(List.of("S -> S a", "S ->")));
                assertTrue(parser.isConflictFree());
                assertTrue(parser.accepts(List.of()));
                assertTrue(parser.accepts(List.of("a", "a", "a")));
            }
        });
    }

    @Test
    void canonicalStatesRetainLookaheadsAndMergingDoesNotMutateThem() {
        Grammar grammar = LRReferenceTablesTest.cc();
        List<Nonterminal> nonterminals = List.copyOf(grammar.nonterminals);
        List<Terminal> terminals = List.copyOf(grammar.terminals);
        LR1 canonical = new LR1(grammar);
        List<Set<ItemLR0>> items = canonical.states().stream().map(s -> Set.copyOf(s.items())).toList();
        List<Map<Symbol, Integer>> transitions = canonical.states().stream()
                .map(s -> Map.copyOf(s.transitions())).toList();
        assertEquals(10, canonical.stateCount());
        assertTrue(canonical.states().stream().flatMap(s -> s.items().stream())
                .allMatch(i -> i instanceof ItemLR1));
        LALR merged = new LALR(canonical);
        assertEquals(7, merged.stateCount());
        assertEquals(items, canonical.states().stream().map(State::items).toList());
        assertEquals(transitions, canonical.states().stream().map(State::transitions).toList());
        assertEquals(7, new LALR(new LR0(grammar)).stateCount());
        assertEquals(10, new LR1(grammar).stateCount());
        assertEquals(nonterminals, grammar.nonterminals);
        assertEquals(terminals, grammar.terminals);
        assertTrue(canonical.accepts(List.of("c", "d", "d")));
        assertTrue(merged.accepts(List.of("c", "d", "d")));
        assertFalse(merged.accepts(List.of("d")));
        assertFalse(merged.accepts(List.of("d", "d", "d")));
    }

    @Test
    void productionIdentityPreservesReduceReduceConflicts() {
        Grammar grammar = new Grammar(List.of("S -> A", "S -> B", "A -> token", "B -> token"));
        for (var factory : PARSERS) {
            AbstractLR parser = factory.apply(grammar);
            assertTrue(parser.conflicts().stream().anyMatch(Conflict::reduceReduce));
            assertNotEquals(parser.ruleNumber(grammar.findNt("A").rules.getFirst()),
                    parser.ruleNumber(grammar.findNt("B").rules.getFirst()));
        }
        LR1 duplicate = new LR1(new Grammar(List.of("S -> token", "S -> token")));
        assertEquals(Set.of(Action.reduce(1), Action.reduce(2)),
                duplicate.conflicts().getFirst().actions());
    }

    @Test
    void lookaheadWordsAreImmutableAndInvalidKIsRejected() {
        Grammar grammar = LRReferenceTablesTest.cc();
        Rule rule = grammar.nonterminals.getFirst().rules.getFirst();
        Sequence sequence = new Sequence(grammar);
        sequence.add(grammar.findT("c").getIndex());
        sequence.add(grammar.findT("d").getIndex());
        List<Integer> original = List.copyOf(sequence);
        ItemLRk item = new ItemLRk(rule, 0, sequence);
        RowLR row = new RowLR();
        row.addAction(sequence, Action.reduce(1));
        sequence.clear();
        assertEquals(original, item.lookahead());
        assertEquals(original, item.goto_().lookahead());
        assertEquals(Set.of(Action.reduce(1)), row.actions(original));
        assertThrows(UnsupportedOperationException.class, () -> item.lookahead().clear());
        assertThrows(UnsupportedOperationException.class, () -> row.actions(original).clear());
        assertThrows(IllegalArgumentException.class, () -> new LRk(grammar, 0));
        assertThrows(IllegalArgumentException.class, () -> new LALRk(grammar, -1));
        assertThrows(IllegalArgumentException.class, () -> new StatesLRk(grammar, 0));
    }

    @Test
    void emptyGrammarIsRejected() {
        for (var factory : PARSERS)
            assertThrows(IllegalArgumentException.class, () -> factory.apply(new Grammar()));
    }

    static class CollidingItem extends ItemLR0 {
        CollidingItem(Rule rule, int dot) { super(rule, dot); }
        @Override public int hashCode() { return 1; }
        @Override public ItemLR0 goto_() { return new CollidingItem(rule, dotPosition + 1); }
    }

    static class CollidingState extends StateLR0 {
        CollidingState(States owner) { super(owner); }
        @Override protected State newState() { return new CollidingState(owner); }
        @Override void add(HashSet<ItemLR0> additions, Rule rule, ItemLR0 from) {
            additions.add(new CollidingItem(rule, 0));
        }
    }

    @Test
    void hashCollisionsAndExistingTransitionsDoNotLoseStates() {
        Grammar grammar = LRReferenceTablesTest.simple();
        LR0 expected = new LR0(grammar);
        States collection = new States(grammar);
        State initial = new CollidingState(collection);
        initial.add(new CollidingItem(collection.startRule, 0));
        AbstractLR actual = new AbstractLR();
        collection.createStates(actual, initial);
        assertEquals(expected.stateCount(), actual.stateCount());
        for (int i = 0; i < expected.stateCount(); i++) {
            assertEquals(expected.states().get(i).core(), actual.states().get(i).core());
            assertEquals(expected.row(i).actions(), actual.row(i).actions());
            assertEquals(expected.row(i).gotos(), actual.row(i).gotos());
        }
        assertTrue(actual.accepts(List.of("id", "+", "id", "+", "id")));
    }
}
