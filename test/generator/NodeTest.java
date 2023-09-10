package generator;

import grammar.Grammar;
import grammar.Symbol;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static java.lang.System.out;
import static org.junit.jupiter.api.Assertions.*;

class NodeTest {
    Grammar grammar1() {
        List<String> lines = new ArrayList<>();
        lines.add("S -> C C");
        lines.add("C -> e C");
        lines.add("C -> d");
        return new Grammar(lines);
    }

    Grammar grammar2() {
        List<String> lines = new ArrayList<>();
        lines.add("S -> a S A");
        lines.add("S ->");
        lines.add("A -> a b S");
        lines.add("A -> c");
        return new Grammar(lines);
    }
    @Test
    void grammar1CTest() {
        Generator generator = new Generator(grammar1());
        Symbol symbol = generator.getNT(1);
        Node node = new Node(generator, symbol, 5);
        assertEquals("d",node.string());
        assertTrue(node.next(5));
        assertEquals("ed",node.string());
        assertTrue(node.next(5));
        assertEquals("C(eC(eC(d)))",node.parenString());
        assertEquals("eed",node.string());
        assertTrue(node.next(5));
        assertEquals("eeed",node.string());
        assertTrue(node.next(5));
        assertEquals("eeeed",node.string());
        assertFalse(node.next(5));
    }

    @Test
    void grammar1Test() {
        Generator generator = new Generator(grammar1());
        Symbol symbol = generator.getNT(0);
        Node node = new Node(generator, symbol,  5);
        assertEquals("dd",node.string());
        assertTrue(node.next(5));
        assertEquals("ded",node.string());
        assertTrue(node.next(5));
        assertEquals("deed",node.string());
        assertTrue(node.next(5));
        assertEquals("deeed",node.string());
        assertTrue(node.next(5));
        assertEquals("edd",node.string());
        assertTrue(node.next(5));
        assertEquals("eded",node.string());
        assertTrue(node.next(5));
        assertEquals("edeed",node.string());
        assertTrue(node.next(5));
        assertEquals("eedd",node.string());
        assertTrue(node.next(5));
        assertEquals("eeded",node.string());
        assertTrue(node.next(5));
        assertEquals("eeedd",node.string());
        node.next(5);
        assertFalse(node.next(5));
    }

    @Test
    void grammar2ATest() {
        Generator generator = new Generator(grammar2());
        Symbol symbol = generator.getNT(1);
        Node node = new Node(generator, symbol, 5);
        assertEquals("c",node.string());
        assertEquals("A(c)",node.parenString());
        assertTrue(node.next(5));
        assertEquals("ab",node.string());
        assertEquals("A(abS())",node.parenString());
        assertTrue(node.next(5));
        assertEquals("abac",node.string());
        assertEquals("A(abS(aS()A(c)))",node.parenString());
        assertTrue(node.next(5));
        assertEquals("abaab",node.string());
        assertEquals("A(abS(aS()A(abS())))",node.parenString());
        assertFalse(node.next(5));
    }

    @Test
    void grammar2Test() {
        Generator generator = new Generator(grammar2());
        Symbol symbol = generator.getNT(0);
        Node node = new Node(generator, symbol, 5);
        assertEquals("",node.string());
        assertEquals("S()",node.parenString());
        assertTrue(node.next(5));
        assertEquals("ac",node.string());
        assertEquals("S(aS()A(c))",node.parenString());
        assertTrue(node.next(5));
        assertEquals("aab",node.string());
        assertEquals("S(aS()A(abS()))",node.parenString());
        assertTrue(node.next(5));
        assertEquals("aabac",node.string());
        assertEquals("S(aS()A(abS(aS()A(c))))",node.parenString());
    }
}