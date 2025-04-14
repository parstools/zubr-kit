package parstools.zubr.grammar;

import java.util.ArrayList;
import java.util.List;

public class TestGrammars {
    public static Grammar tokenGrammar() {
        List<String> lines = new ArrayList<>();
        lines.add("S -> a b c d e f g");
        return new Grammar(lines);
    }

    public static Grammar grammar1() {
        List<String> lines = new ArrayList<>();
        lines.add("S -> C C");
        lines.add("C -> e C");
        lines.add("C -> d");
        return new Grammar(lines);
    }

    public static Grammar grammar2() {
        List<String> lines = new ArrayList<>();
        lines.add("S -> a S A");
        lines.add("S ->");
        lines.add("A -> a b S");
        lines.add("A -> c");
        return new Grammar(lines);
    }

    public static Grammar grammar3() {
        List<String> lines = new ArrayList<>();
        lines.add("S -> C C C");
        lines.add("C -> a");
        lines.add("C -> b");
        return new Grammar(lines);
    }

    public static Grammar grammar4() {
        List<String> lines = new ArrayList<>();
        lines.add("S -> a S A");
        lines.add("S ->");
        lines.add("A -> a b S");
        lines.add("A -> c");
        return new Grammar(lines);
    }

    // bad grammar
    public static Grammar grammar5() {
        List<String> lines = new ArrayList<>();
        lines.add("A -> B");
        lines.add("A -> b B");
        lines.add("B -> A");
        lines.add("B -> a A");
        return new Grammar(lines);
    }

    public static Grammar grammar6() {
        List<String> lines = new ArrayList<>();
        lines.add("S -> S S");
        lines.add("S -> i");
        return new Grammar(lines);
    }

    public static Grammar canonicalForm() {
        List<String> lines = new ArrayList<>();
        lines.add("E -> E + T");
        lines.add("E -> T");
        lines.add("T -> T * F");
        lines.add("T -> F");
        lines.add("F -> ( E )");
        lines.add("F -> i");
        return new Grammar(lines);
    }

    public static Grammar shuffled() {
        List<String> lines = new ArrayList<>();
        lines.add("E -> T");
        lines.add("E -> E + T");
        lines.add("F -> ( E )");
        lines.add("F -> i");
        lines.add("T -> F");
        lines.add("T -> T * F");
        return new Grammar(lines);
    }

    public static Grammar grammar7() {
        List<String> lines = new ArrayList<>();
        lines.add("S -> M");
        lines.add("S -> U");
        lines.add("M -> i e t M e M");
        lines.add("M -> o");
        lines.add("U -> i e t S");
        lines.add("U -> i e t M e U");
        return new Grammar(lines);
    }

    public static Grammar grammar8() {
        List<String> lines = new ArrayList<>();
        lines.add("X -> Y");
        lines.add("X -> b Y a");
        lines.add("Y -> c");
        lines.add("Y -> c a");
        return new Grammar(lines);
    }

    public static Grammar grammar9() {
        List<String> lines = new ArrayList<>();
        lines.add("S -> i e t S");
        lines.add("S -> i e t W e S");
        lines.add("S -> o");
        lines.add("W -> i e t W e W");
        lines.add("W -> o");
        return new Grammar(lines);
    }

    public static Grammar cycle1() {
        List<String> lines = new ArrayList<>();
        lines.add("A -> A");
        lines.add("A -> a");
        return new Grammar(lines);
    }

    public static Grammar cycle2() {
        List<String> lines = new ArrayList<>();
        lines.add("A -> B");
        lines.add("A -> a");
        lines.add("A -> c B");
        lines.add("B -> A");
        lines.add("B -> b");
        return new Grammar(lines);
    }

    public static Grammar cycle112() {
        List<String> lines = new ArrayList<>();
        lines.add("A -> A");
        lines.add("B -> B");
        lines.add("A -> B");
        lines.add("A -> a");
        lines.add("A -> c B");
        lines.add("B -> A");
        lines.add("B -> b");
        return new Grammar(lines);
    }

    public static Grammar cycle3() {
        List<String> lines = new ArrayList<>();
        lines.add("A -> B");
        lines.add("A -> a");
        lines.add("B -> C");
        lines.add("B -> b");
        lines.add("C -> A");
        lines.add("C -> c");
        return new Grammar(lines);
    }

    public static Grammar cycle32() {
        List<String> lines = new ArrayList<>();
        lines.add("A -> B");
        lines.add("A -> D");
        lines.add("A -> a");
        lines.add("B -> C");
        lines.add("B -> b");
        lines.add("C -> A");
        lines.add("C -> c");
        lines.add("D -> E");
        lines.add("D -> d");
        lines.add("E -> D");
        lines.add("E -> e");
        return new Grammar(lines);
    }

    public static Grammar cycle2wide() {
        List<String> lines = new ArrayList<>();
        lines.add("A -> B C");
        lines.add("A -> a");
        lines.add("B -> A C");
        lines.add("B -> b");
        lines.add("C ->");
        lines.add("C -> c");
        return new Grammar(lines);
    }

    public static Grammar cycle3with2() {
        List<String> lines = new ArrayList<>();
        lines.add("A -> B D");
        lines.add("A -> a");
        lines.add("B -> C");
        lines.add("B -> b");
        lines.add("C -> A");
        lines.add("C -> c");
        lines.add("D ->E");
        lines.add("D ->");
        lines.add("E ->D");
        return new Grammar(lines);
    }

    public static Grammar cycle332() {
        List<String> lines = new ArrayList<>();
        lines.add("A -> B E");
        lines.add("A -> a");
        lines.add("B -> C");
        lines.add("B -> b");
        lines.add("C -> A");
        lines.add("C -> c");
        lines.add("C ->");
        lines.add("D -> E");
        lines.add("E ->D C");
        lines.add("E ->");
        return new Grammar(lines);
    }

    public static Grammar cycle31() {
        List<String> lines = new ArrayList<>();
        lines.add("A -> B D");
        lines.add("A -> a");
        lines.add("B -> C");
        lines.add("B -> b");
        lines.add("C -> A");
        lines.add("C -> F");
        lines.add("C -> c");
        lines.add("D ->E");
        lines.add("D ->");
        lines.add("E ->D C");
        lines.add("F ->F");
        lines.add("F ->f");
        return new Grammar(lines);
    }

    public static Grammar cycle31a() {
        List<String> lines = new ArrayList<>();
        lines.add("A -> B D");
        lines.add("A -> a");
        lines.add("B -> C F");
        lines.add("B -> b");
        lines.add("C -> A");
        lines.add("C -> c");
        lines.add("D ->E");
        lines.add("D ->");
        lines.add("E ->D C");
        lines.add("F -> F");
        lines.add("F ->");
        return new Grammar(lines);
    }

    public static Grammar cycle322() {
        List<String> lines = new ArrayList<>();
        lines.add("A -> B D");
        lines.add("A -> a");
        lines.add("B -> C F");
        lines.add("B -> b");
        lines.add("C -> A");
        lines.add("C -> c");
        lines.add("D ->E");
        lines.add("D ->");
        lines.add("E ->D C");
        lines.add("F -> B");
        lines.add("F ->");
        return new Grammar(lines);
    }

    public static Grammar leftRecursive() {
        List<String> lines = new ArrayList<>();
        lines.add("E -> E + T");
        lines.add("E -> T");
        lines.add("T -> T ∗ F");
        lines.add("T -> F");
        lines.add("F -> ( E )");
        lines.add("F -> i");
        return new Grammar(lines);
    }
    public static Grammar stdLL1() {
        List<String> lines = new ArrayList<>();
        lines.add("E -> T E1");
        lines.add("E1 -> + T E1");
        lines.add("E1 ->");
        lines.add("T -> F T1");
        lines.add("T1 -> * F T1");
        lines.add("T1 ->");
        lines.add("F -> ( E )");
        lines.add("F -> i");
        return new Grammar(lines);
    }

    public static Grammar LL1() {
        List<String> lines = new ArrayList<>();
        lines.add("S -> a S A");
        lines.add("S ->");
        lines.add("A -> b S");
        lines.add("A -> c");
        return new Grammar(lines);
    }

    public static Grammar LL2() {
        List<String> lines = new ArrayList<>();
        lines.add("S -> a S A");
        lines.add("S ->");
        lines.add("A -> a b S");
        lines.add("A -> c");
        return new Grammar(lines);
    }

    public static Grammar LL3() {
        List<String> lines = new ArrayList<>();
        lines.add("S -> a S A");
        lines.add("S ->");
        lines.add("A -> a a b S");
        lines.add("A -> c");
        return new Grammar(lines);
    }

    public static Grammar LLstar() {
        List<String> lines = new ArrayList<>();
        lines.add("S -> a S C");
        lines.add("S ->");
        lines.add("C -> A b S");
        lines.add("C -> c");
        lines.add("A -> a A");
        lines.add("A ->");
        return new Grammar(lines);
    }

    public static Grammar LLstar1() {
        List<String> lines = new ArrayList<>();
        lines.add("S -> A B A");
        lines.add("S ->");
        lines.add("A -> a A");
        lines.add("A ->");
        lines.add("B -> b B");
        lines.add("B ->");
        return new Grammar(lines);
    }

    public static Grammar factor() {
        List<String> lines = new ArrayList<>();
        lines.add("S -> i E t S");
        lines.add("S -> a");
        lines.add("S -> a E");
        lines.add("S -> i E t a");
        lines.add("S -> b E");
        lines.add("S -> i E t S e S");
        lines.add("S -> i E i a");
        lines.add("E -> e");
        return new Grammar(lines);
    }

    //https://pl.wikipedia.org/wiki/Generowanie_parser%C3%B3w_LR
    public static Grammar LRwikiLR0() {
        List<String> lines = new ArrayList<>();
        lines.add("E -> E * B");
        lines.add("E -> E + B");
        lines.add("E -> B");
        lines.add("B -> 0");
        lines.add("B -> 1");
        return new Grammar(lines);
    }

    public static Grammar LRwikiSLR() {
        List<String> lines = new ArrayList<>();
        lines.add("E -> A 1");
        lines.add("E -> B 2");
        lines.add("A -> 1");
        lines.add("B -> 1");
        return new Grammar(lines);
    }

    public static Grammar LRwikiLR1() {
        List<String> lines = new ArrayList<>();
        lines.add("S -> L = R");
        lines.add("S -> R");
        lines.add("L -> * R");
        lines.add("L -> i");
        lines.add("R -> L");
        return new Grammar(lines);
    }

    public static Grammar generatorRecursion() {
        List<String> lines = new ArrayList<>();
        lines.add("S -> a");
        lines.add("S -> S a");
        return new Grammar(lines);
    }

    public static Grammar generatorStack() {
        List<String> lines = new ArrayList<>();
        lines.add("S -> A A");
        lines.add("S -> c");
        lines.add("S -> b B");
        lines.add("A ->");
        lines.add("B -> S A B");
        lines.add("B -> S c c");
        lines.add("B -> B A");
        return new Grammar(lines);
    }

    public static Grammar generatorStackOverflow() {
        List<String> lines = new ArrayList<>();
        lines.add("S -> b A");
        lines.add("S ->");
        lines.add("S -> A A");
        lines.add("A -> a A");
        lines.add("A -> b A");
        lines.add("A -> S S S");
        return new Grammar(lines);
    }

    public static Grammar generatorStack1() {
        List<String> lines = new ArrayList<>();
        lines.add("S -> A A A");
        lines.add("S ->");
        lines.add("S -> S a c");
        lines.add("A -> B b");
        lines.add("A -> a");
        lines.add("A -> B S");
        lines.add("B -> c B");
        lines.add("B ->");
        lines.add("B -> a a a");
        return new Grammar(lines);
    }

    public static Grammar eliminateRecursionHalt() {
        List<String> lines = new ArrayList<>();
        lines.add("S -> c B c");
        lines.add("S -> A a A");
        lines.add("S -> b B");
        lines.add("A -> B b");
        lines.add("B -> S c");
        lines.add("B -> A b a");
        lines.add("B -> b c");
        return new Grammar(lines);
    }

    public static Grammar LR2_LL3_noSLL() {
        List<String> lines = new ArrayList<>();
        lines.add("X -> Y");
        lines.add("X -> b Y a");
        lines.add("Y -> c");
        lines.add("Y -> c a");
        return new Grammar(lines);
    }
}
