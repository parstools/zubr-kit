package parstools.zubr.generator;

import org.junit.jupiter.api.Test;
import parstools.zubr.grammar.Grammar;
import parstools.zubr.grammar.TestGrammars;
import parstools.zubr.set.Sequence;
import parstools.zubr.set.SequenceSet;
import parstools.zubr.set.TokenSet;

import java.util.Stack;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CollectTest {
    @Test
    void collectFirst() {
        Grammar grammar = TestGrammars.grammar2();
        Generator generator = new Generator(grammar, 5, RuleOrder.roSort);
        generator.createRootFromString("S(aS()S(b))");
        SequenceSet sset1 = new SequenceSet();
        SequenceSet expected1 = new SequenceSet();
        expected1.add(new Sequence(grammar, "a"));
        expected1.add(new Sequence(grammar, "b"));
        expected1.add(new Sequence(grammar, ""));
        generator.root.collectFirst(0,1, sset1);
        assertEquals(expected1, sset1);
        SequenceSet sset2 = new SequenceSet();
        SequenceSet expected2 = new SequenceSet();
        expected2.add(new Sequence(grammar, "ab"));
        expected2.add(new Sequence(grammar, "b"));
        expected2.add(new Sequence(grammar, ""));
        generator.root.collectFirst(0,2, sset2);
        assertEquals(expected2, sset2);
    }

    @Test
    void terminalsFrom() {
        Grammar grammar = TestGrammars.grammar2();
        Generator generator = new Generator(grammar, 5, RuleOrder.roSort);
        generator.createRootFromString("S(aS()S(b)a)");
        Sequence seq = generator.root.terminalsFrom(2,1);
        assertEquals("b", seq.toString());
        seq = generator.root.terminalsFrom(3,1);
        assertEquals("a", seq.toString());

        seq = generator.root.terminalsFrom(2,2);
        assertEquals("b a", seq.toString());
        seq = generator.root.terminalsFrom(3,2);
        assertEquals("a", seq.toString());

        seq = generator.root.terminalsFrom(2,3);
        assertEquals("b a", seq.toString());
        seq = generator.root.terminalsFrom(3,3);
        assertEquals("a", seq.toString());
    }

    @Test
    void collectFollow() {
        Grammar grammar = TestGrammars.grammar2();
        Generator generator = new Generator(grammar, 5, RuleOrder.roSort);
        Sequence upSeq = new Sequence(grammar, "$");
        Stack<Sequence> stackSeq = new Stack<>();
        stackSeq.add(upSeq);
        generator.createRootFromString("S(A(S(c)ab))");

        SequenceSet sset1 = new SequenceSet();
        SequenceSet expected1 = new SequenceSet();
        expected1.add(new Sequence(grammar, "$"));
        expected1.add(new Sequence(grammar, "a"));
        generator.root.collectFollow(0,1, stackSeq, sset1);
        assertEquals(expected1, sset1);

        SequenceSet sset2 = new SequenceSet();
        SequenceSet expected2 = new SequenceSet();
        expected2.add(new Sequence(grammar, "$"));
        expected2.add(new Sequence(grammar, "ab"));
        generator.root.collectFollow(0,2, stackSeq, sset2);
        assertEquals(expected2, sset2);

        SequenceSet sset3 = new SequenceSet();
        SequenceSet expected3 = new SequenceSet();
        expected3.add(new Sequence(grammar, "$"));
        expected3.add(new Sequence(grammar, "ab$"));
        generator.root.collectFollow(0,3, stackSeq, sset3);
        assertEquals(expected3, sset3);

        SequenceSet sset4 = new SequenceSet();
        SequenceSet expected4 = new SequenceSet();
        expected4.add(new Sequence(grammar, "$"));
        expected4.add(new Sequence(grammar, "ab$"));
        generator.root.collectFollow(0,4, stackSeq, sset4);
        assertEquals(expected4, sset4);
    }

    @Test
    void collectFollow2() {
        Grammar grammar = TestGrammars.grammar2();
        Generator generator = new Generator(grammar, 5, RuleOrder.roSort);
        Sequence upSeq = new Sequence(grammar, "$");
        Stack<Sequence> stackSeq = new Stack<>();
        stackSeq.add(upSeq);
        generator.createRootFromString("S(A(S(c)ab)S(S(c))b)");

        SequenceSet sset2 = new SequenceSet();
        SequenceSet expected2 = new SequenceSet();
        expected2.add(new Sequence(grammar, "$"));
        expected2.add(new Sequence(grammar, "b$"));
        expected2.add(new Sequence(grammar, "ab"));
        generator.root.collectFollow(0, 2, stackSeq, sset2);
        assertEquals(expected2, sset2);
    }

    @Test
    void grammar2Collect() {
        TokenSet firstS,firstA;
        TokenSet followS, followA;
        Generator generator = new Generator(TestGrammars.grammar2(), 5, RuleOrder.roSort);
        assertTrue(generator.next());
        assertEquals("",generator.string());
        assertEquals("S()",generator.parenString());
        for (int k=1; k<3; k++) {
            firstS = generator.collectFirst(0, k);
            firstA = generator.collectFirst(1, k);
            followS = generator.collectFollow(0, k);
            followA = generator.collectFollow(1, k);
            assertEquals("{eps}", firstS.toString());
            assertEquals("{}", firstA.toString());
            assertEquals("{$}", followS.toString());
            assertEquals("{}", followA.toString());
        }
        assertTrue(generator.next());
        assertEquals("ac",generator.string());
        assertEquals("S(aS()A(c))",generator.parenString());

        firstS = generator.collectFirst(0, 1);
        firstA = generator.collectFirst(1, 1);
        followS = generator.collectFollow(0, 1);
        followA = generator.collectFollow(1, 1);
        assertEquals("{eps a}", firstS.toString());
        assertEquals("{c}", firstA.toString());
        assertEquals("{c $}", followS.toString());
        assertEquals("{$}", followA.toString());

        firstS = generator.collectFirst(0, 2);
        firstA = generator.collectFirst(1, 2);
        followS = generator.collectFollow(0, 2);
        followA = generator.collectFollow(1, 2);
        assertEquals("{eps ac}", firstS.toString());
        assertEquals("{c}", firstA.toString());
        assertEquals("{$ c$}", followS.toString());
        assertEquals("{$}", followA.toString());

        assertTrue(generator.next());
        assertEquals("aab",generator.string());
        assertEquals("S(aS()A(abS()))",generator.parenString());

        firstS = generator.collectFirst(0, 1);
        firstA = generator.collectFirst(1, 1);
        followS = generator.collectFollow(0, 1);
        followA = generator.collectFollow(1, 1);
        assertEquals("{eps a}", firstS.toString());
        assertEquals("{a}", firstA.toString());
        assertEquals("{a $}", followS.toString());
        assertEquals("{$}", followA.toString());

        firstS = generator.collectFirst(0, 2);
        firstA = generator.collectFirst(1, 2);
        followS = generator.collectFollow(0, 2);
        followA = generator.collectFollow(1, 2);
        assertEquals("{eps aa}", firstS.toString());
        assertEquals("{ab}", firstA.toString());
        assertEquals("{ab $}", followS.toString());
        assertEquals("{$}", followA.toString());

        assertTrue(generator.next());
        assertEquals("aabac",generator.string());
        assertEquals("S(aS()A(abS(aS()A(c))))",generator.parenString());

        firstS = generator.collectFirst(0, 1);
        firstA = generator.collectFirst(1, 1);
        followS = generator.collectFollow(0, 1);
        followA = generator.collectFollow(1, 1);
        assertEquals("{eps a}", firstS.toString());
        assertEquals("{a c}", firstA.toString());
        assertEquals("{a c $}", followS.toString());
        assertEquals("{$}", followA.toString());

        firstS = generator.collectFirst(0, 2);
        firstA = generator.collectFirst(1, 2);
        followS = generator.collectFollow(0, 2);
        followA = generator.collectFollow(1, 2);
        assertEquals("{eps aa ac}", firstS.toString());
        assertEquals("{c ab}", firstA.toString());
        assertEquals("{ab $ c$}", followS.toString());
        assertEquals("{$}", followA.toString());

        firstS = generator.collectFirst(0, 3);
        firstA = generator.collectFirst(1, 3);
        followS = generator.collectFollow(0, 3);
        followA = generator.collectFollow(1, 3);

        assertEquals("{eps ac aab}", firstS.toString());
        assertEquals("{c aba}", firstA.toString());
        assertEquals("{aba $ c$}", followS.toString());
        assertEquals("{$}", followA.toString());
    }
}
