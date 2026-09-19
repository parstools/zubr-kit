package parstools.zubr;

import parstools.zubr.generator.Generator;
import parstools.zubr.generator.RuleOrder;
import parstools.zubr.grammar.Grammar;
import parstools.zubr.ll.ParsingTable;
import parstools.zubr.lr.LALR;
import parstools.zubr.lr.LALRk;
import parstools.zubr.lr.LR0;
import parstools.zubr.lr.LR1;
import parstools.zubr.lr.LRk;
import parstools.zubr.lr.SLR;
import parstools.zubr.set.Sequence;
import parstools.zubr.set.SetContainer;
import parstools.zubr.set.TokenSet;
import parstools.zubr.util.NoMinLenGrammarException;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import static java.lang.System.out;

public class Main {
    private static final int MAX_LOOKAHEAD = 6;
    private static final long CLASSIFICATION_TIMEOUT_NANOS = Duration.ofSeconds(30).toNanos();
    private static final Pattern GRAMMAR_LABEL = Pattern.compile(
            "(?:\\[(?:notLALR|LALR|notLR|LR|SLR|notLL|LL)[^\\]\\t ]{0,5}\\]?|\\[ambig\\])[ \\t]?");

    static void makeKTest(List<String> lines) throws IOException {
        int n = 0;
        int counter = 0;
        int counterFailed = 0;
        long start = System.nanoTime();
        FileWriter fileWriter = new FileWriter("k4.dat");
        PrintWriter printWriter = new PrintWriter(fileWriter);
        while (n < lines.size()) {
            List<String> gramLines = new ArrayList<>();
            n = readGramLines(lines, n, gramLines);
            Grammar grammar = new Grammar(gramLines);
            SetContainer sc1 = new SetContainer(grammar);
            out.println(counter);
            sc1.reset(1);
            sc1.computeSetsByGeneration(1, 20, 1000);
            counter++;
            n++;
            List<String> expectLines = new ArrayList<>();
            n = readExpectLines(lines, n, expectLines);
            SetContainer sc2 = new SetContainer(grammar);
            sc2.readTest1(expectLines);
            if (sc1.hashCode() == sc2.hashCode()) {
                out.println("OK");
                for (String line : gramLines)
                    printWriter.println(line);
                int limit = 2000;
                for (int k = 1; k <= 4; k++) {
                    printWriter.println("===" + k);
                    SetContainer sc = new SetContainer(grammar);
                    sc.reset(k);
                    sc.computeSetsByRangeGeneration(k, 4, 20, limit);
                    sc.dump(printWriter);
                    limit *= 2;
                }
                printWriter.println();
            } else {
                counterFailed++;
                out.println(sc1);
                out.println(sc2);
                out.println("differ..");
            }
            n++;
        }
        printWriter.close();
        out.println(counter);
        out.println("failed " + counterFailed);
        long duration = System.nanoTime() - start;
        out.println("duration=" + duration / 1e9);
    }

    static void nextCounter(List<String> lines) {
        int n = 0;
        int count = 0;
        int countNoMin = 0;
        long start = System.nanoTime();
        while (n < lines.size()) {
            List<String> gramLines = new ArrayList<>();
            n = readGramLines(lines, n, gramLines);
            try {
                Grammar grammar = new Grammar(gramLines);
                int limit = 1000 * 1000;
                gramLines.forEach(out::println);
                out.println("---");
                for (int maxLen = 1; maxLen <= 64; maxLen++) {
                    Generator generator = new Generator(grammar, maxLen, RuleOrder.roSort);
                    int nc = 0;
                    while (generator.next()) {
                        nc++;
                        if (nc >= limit)
                            break;
                    }
                    out.println(maxLen + ": " + nc);
                    if (nc == limit)
                        break;
                }
                out.println();
            } catch (NoMinLenGrammarException e) {
                countNoMin++;
            }
            n++;
            count++;
            List<String> expectLines = new ArrayList<>();
            n = readExpectLines(lines, n, expectLines);
            n++;
        }
        out.println(count + " grammars, " + countNoMin + " exceptions");
        long duration = System.nanoTime() - start;
        out.println("duration=" + duration / 1e9);
    }

    static void testK4grammars() throws IOException {
        Path path = Paths.get("res/k4.dat");
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        int n = 0;
        int count = 0;
        while (n < lines.size()) {
            List<String> gramLines = new ArrayList<>();
            n = readGramLines(lines, n, gramLines);
            Grammar grammar = new Grammar(gramLines);
            count++;
            List<String> expectLines = new ArrayList<>();
            n = readExpectLines(lines, n, expectLines);
            testK4grammar(grammar, expectLines);
            n++;
        }
        out.println(count + " grammars");
    }

    static int testAmbig(Grammar grammar, List<String> ambigInfo) {
        for (int len = 1; len <=12; len++) {
            int res = testAmbig(grammar, len, 5000, ambigInfo);
            if (res < 0)
                return res;
        }
        return 1;
    }

    static int testAmbig(Grammar grammar, int maxLen, int limit, List<String> ambigInfo) {
        ambigInfo.clear();
        Generator generator = new Generator(grammar, maxLen, RuleOrder.roSort);
        int counter = 0;
        boolean broken = false;
        boolean ambig = false;
        Map<Sequence, String> smap = new HashMap<>();
        while (generator.next()) {
            counter++;
            Sequence seq = generator.seq();
            String paren = generator.parenString();
            if (smap.containsKey(seq)) {
                ambigInfo.add(seq.toString());
                String prev = smap.get(seq);
                ambigInfo.add(prev);
                ambigInfo.add(paren);
                return -1;
            }
            //smap.put(seq, paren);
            smap.put(seq,paren);
            if (counter >= limit) {
                broken = true;
                break;
            }
        }
        if (broken) return 0;
        else return 1;
    }

    static void readCMinus() throws IOException {
        Path path = Paths.get("src/main/resources/CMinus.dat");
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        Grammar grammar = new Grammar(lines);
        grammar.eliminationRecursion();
        if (grammar.stayRecursion)
            out.println("recursive");
        grammar.factorization(1);
        List<String> lines1 = grammar.toList();
        lines1.forEach(System.out::println);
        ParsingTable table = new ParsingTable(grammar);
        int resk = -1;
        for (int k = 1; k <= 4; k++) {
            boolean res = table.createLL(k);
            if (res) {
                resk = k;
                break;
            }
        }
        out.println("k=" + resk);
    }

    static void readAllGrammars() throws IOException {
        Path path = Paths.get("src/main/resources/grammars.dat");
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        int n = 0;
        int counter = 0;
        while (n < lines.size()) {
            List<String> gramLines = new ArrayList<>();
            n = readGramLines(lines, n, gramLines);
            Grammar grammar = new Grammar(gramLines);
            List<String> ambigInfo = new ArrayList<>();
            int test = testAmbig(grammar, ambigInfo);
            if(test<0) { //ambiguous
                n++;
                continue;
            } else {
                out.printf("%d: ", counter);
                //gramLines.forEach(out::println);
                grammar.eliminationRecursion();
                if (grammar.stayRecursion)
                    out.println("recursive");
                else {
                    grammar.factorization(1);
                    ParsingTable table = new ParsingTable(grammar);
                    int resk = -1;
                    for (int k = 1; k <= MAX_LOOKAHEAD; k++) {
                        boolean res = table.createLL(k);
                        if (res) {
                            resk = k;
                            break;
                        }
                    }
                    if (resk >= 1) {
                        out.printf("LL(%d)%n", resk);
                    } else
                        out.println("no LL(k)");
                }
            }
            counter++;
            n++;
        }
        out.println(counter + " grammars");
    }

    /**
     * Classifies every grammar in grammars.dat and writes the preserved source,
     * with refreshed labels, to grammars_result.dat.
     */
    static void labelGrammars() throws IOException {
        Path input = Paths.get("src/main/resources/grammars.dat");
        Path output = Paths.get("src/main/resources/grammars_result.dat");
        List<String> lines = Files.readAllLines(input, StandardCharsets.UTF_8);
        List<String> result = new ArrayList<>();
        int totalGrammars = countNonEmptyBlocks(lines);
        Map<String, Integer> summary = new LinkedHashMap<>();
        summary.put("[ambig]", 0);
        summary.put("[LR(0)]", 0);
        summary.put("[SLR]", 0);
        for (int k = 1; k <= MAX_LOOKAHEAD; k++)
            summary.put("[LR(" + k + ")]", 0);
        for (int k = 1; k <= MAX_LOOKAHEAD; k++)
            summary.put("[LALR(" + k + ")]", 0);
        for (int k = 1; k <= MAX_LOOKAHEAD; k++)
            summary.put("[notLALR(" + k + ")]", 0);
        for (int k = 1; k <= MAX_LOOKAHEAD; k++)
            summary.put("[notLR(" + k + ")]", 0);
        for (int k = 1; k <= MAX_LOOKAHEAD; k++)
            summary.put("[LL(" + k + ")]", 0);
        for (int k = 1; k <= MAX_LOOKAHEAD; k++)
            summary.put("[notLL(" + k + ")]", 0);
        int grammarCount = 0;
        int n = 0;
        while (n < lines.size()) {
            if (lines.get(n).trim().isEmpty()) {
                result.add(lines.get(n++));
                continue;
            }
            List<String> block = new ArrayList<>();
            while (n < lines.size() && !lines.get(n).trim().isEmpty())
                block.add(lines.get(n++));
            out.printf("%d/%d", grammarCount + 1, totalGrammars);
            out.flush();
            List<String> labeled = labelGrammar(block, stage -> {
                out.print(" " + stage);
                out.flush();
            });
            result.addAll(labeled);
            String firstComment = firstComment(labeled);
            summary.replaceAll((label, count) -> firstComment.contains(label) ? count + 1 : count);
            grammarCount++;
            out.println();
        }
        Files.write(output, result, StandardCharsets.UTF_8);
        out.println(grammarCount + " grammars written to " + output);
        out.println("summary:");
        summary.forEach((label, count) -> {
            if (count > 0)
                out.printf("%s: %d%n", label.substring(1, label.length() - 1), count);
        });
    }

    private static int countNonEmptyBlocks(List<String> lines) {
        int count = 0;
        boolean insideBlock = false;
        for (String line : lines) {
            if (line.trim().isEmpty()) {
                insideBlock = false;
            } else if (!insideBlock) {
                count++;
                insideBlock = true;
            }
        }
        return count;
    }

    private static String firstComment(List<String> block) {
        return block.stream()
                .map(String::trim)
                .filter(line -> line.startsWith(";"))
                .findFirst()
                .orElse("");
    }

    static List<String> labelGrammar(List<String> block) {
        return labelGrammar(block, ignored -> {});
    }

    private static List<String> labelGrammar(List<String> block, Consumer<String> progress) {
        List<String> cleaned = new ArrayList<>(block.size() + 1);
        List<String> grammarLines = new ArrayList<>();
        int firstComment = -1;
        for (String line : block) {
            if (line.trim().startsWith(";")) {
                if (firstComment < 0)
                    firstComment = cleaned.size();
                cleaned.add(cleanGrammarLabels(line));
            } else {
                cleaned.add(line);
                grammarLines.add(line.trim());
            }
        }
        if (grammarLines.isEmpty())
            return cleaned;

        Grammar ambiguityGrammar = new Grammar(grammarLines);
        List<String> ambigInfo = new ArrayList<>();
        String labels;
        if (testAmbig(ambiguityGrammar, ambigInfo) < 0) {
            progress.accept("ambig");
            labels = "[ambig]";
        } else {
            Grammar lrGrammar = new Grammar(grammarLines);
            CanonicalLRs canonical = new CanonicalLRs(lrGrammar);
            long lrStartedAt = System.nanoTime();
            String lrLabel = classifyLR(lrGrammar, canonical, lrStartedAt, progress);
            String lalrLabel = classifyLALR(lrLabel, canonical, progress);
            String llLabel = classifyLL(new Grammar(grammarLines), progress);
            labels = String.join(" ", lalrLabel.isEmpty()
                    ? List.of(lrLabel, llLabel)
                    : List.of(lrLabel, lalrLabel, llLabel));
        }

        if (firstComment < 0) {
            cleaned.addFirst(";" + labels);
        } else {
            String comment = cleaned.get(firstComment);
            String text = comment.substring(comment.indexOf(';') + 1);
            String separator = text.isEmpty() || Character.isWhitespace(text.charAt(0)) ? "" : " ";
            cleaned.set(firstComment, ";" + labels + separator + text);
        }
        return cleaned;
    }

    static String cleanGrammarLabels(String comment) {
        return GRAMMAR_LABEL.matcher(comment).replaceAll("");
    }

    private static String classifyLR(Grammar grammar, CanonicalLRs canonical,
                                     long startedAt, Consumer<String> progress) {
        progress.accept("LR(0)");
        if (new LR0(grammar).isConflictFree())
            return "[LR(0)]";
        progress.accept("SLR");
        if (new SLR(grammar).isConflictFree())
            return "[SLR]";
        progress.accept("LR(1)");
        if (canonical.lr1().isConflictFree())
            return "[LR(1)]";
        int lastTestedK = 1;
        for (int k = 2; k <= MAX_LOOKAHEAD; k++) {
            if (classificationTimedOut(startedAt))
                break;
            progress.accept("LR(" + k + ")");
            LRk lr = canonical.lrk(k);
            lastTestedK = k;
            if (lr.isConflictFree())
                return "[LR(" + k + ")]";
        }
        return "[notLR(" + lastTestedK + ")]";
    }

    private static String classifyLALR(String lrLabel, CanonicalLRs canonical,
                                       Consumer<String> progress) {
        int k = lalrLookahead(lrLabel);
        if (k < 1)
            return "";
        progress.accept("LALR(" + k + ")");
        boolean conflictFree = k == 1
                ? new LALR(canonical.lr1()).isConflictFree()
                : new LALRk(canonical.lrk(k)).isConflictFree();
        return conflictFree ? "[LALR(" + k + ")]" : "[notLALR(" + k + ")]";
    }

    private static int lalrLookahead(String lrLabel) {
        if (lrLabel.equals("[LR(0)]") || lrLabel.equals("[SLR]") || lrLabel.equals("[LR(1)]"))
            return 1;
        for (int k = 2; k <= MAX_LOOKAHEAD; k++) {
            if (lrLabel.equals("[LR(" + k + ")]"))
                return k;
        }
        return -1;
    }

    private static final class CanonicalLRs {
        private final Grammar grammar;
        private final Map<Integer, LRk> byK = new HashMap<>();
        private LR1 lr1;

        private CanonicalLRs(Grammar grammar) {
            this.grammar = grammar;
        }

        private LR1 lr1() {
            if (lr1 == null)
                lr1 = new LR1(grammar);
            return lr1;
        }

        private LRk lrk(int k) {
            LRk parser = byK.get(k);
            if (parser == null) {
                parser = new LRk(grammar, k);
                byK.put(k, parser);
            }
            return parser;
        }
    }

    private static boolean classificationTimedOut(long startedAt) {
        return System.nanoTime() - startedAt >= CLASSIFICATION_TIMEOUT_NANOS;
    }

    private static String classifyLL(Grammar grammar, Consumer<String> progress) {
        progress.accept("recursion");
        grammar.eliminationRecursion();
        if (grammar.stayRecursion)
            return "[notLL(" + MAX_LOOKAHEAD + ")]";
        progress.accept("factorization");
        grammar.factorization(1);
        ParsingTable table = new ParsingTable(grammar);
        long startedAt = System.nanoTime();
        int lastTestedK = 0;
        for (int k = 1; k <= MAX_LOOKAHEAD; k++) {
            if (classificationTimedOut(startedAt))
                break;
            progress.accept("LL(" + k + ")");
            boolean isLL = table.createLL(k);
            lastTestedK = k;
            if (isLL)
                return "[LL(" + k + ")]";
        }
        return "[notLL(" + Math.max(lastTestedK, 1) + ")]";
    }

    private static void testK4grammar(Grammar grammar, List<String> expectLines) {
        int n = 0;
        while ((n = testK(grammar, n, expectLines)) < expectLines.size()) {
        }
    }

    private static int testK(Grammar grammar, int n, List<String> lines) {
        int failed = 0;
        String line = lines.get(n).trim();
        assert (line.startsWith("==="));
        int k = Integer.valueOf(line.substring(3, line.length()));
        SetContainer sc = new SetContainer(grammar);
        sc.reset(k);
        sc.makeFirstSetsK(k);
        sc.makeFollowSetsK(k);
        n++;
        line = lines.get(n).trim();
        assert (line.equals("FIRST:"));
        n++;
        for (int i = 0; i < sc.firstSets.size(); i++) {
            line = lines.get(n).trim();
            if (!compareSetWithLine(grammar, k, sc.firstSets.get(i), line))
                failed++;
            n++;
        }
        line = lines.get(n).trim();
        assert (line.equals("FOLLOW:"));
        n++;
        for (int i = 0; i < sc.followSets.size(); i++) {
            line = lines.get(n).trim();
            if (!compareSetWithLine(grammar, k, sc.followSets.get(i), line))
                failed++;
            n++;
        }
        return n;
    }

    private static boolean compareSetWithLine(Grammar grammar, int k, TokenSet tokenSet, String line) {
        int pos = line.indexOf("{");
        String tokenStr = line.substring(pos, line.length());
        TokenSet expectSet = new TokenSet(grammar, k);
        expectSet.parse(tokenStr);
        boolean eq = expectSet.toString().equals(tokenSet.toString());
        if (!eq) {
            out.println("differ " + expectSet.toString() + " " + tokenSet.toString());
        }
        return eq;
    }

    static void detectCycles(List<String> lines) {
        int n = 0;
        int count = 0;
        int countNoMin = 0;
        while (n < lines.size()) {
            List<String> gramLines = new ArrayList<>();
            n = readGramLines(lines, n, gramLines);
            try {
                Grammar grammar = new Grammar(gramLines);
                gramLines.forEach(out::println);
                Generator generator = new Generator(grammar, 5, RuleOrder.roOriginal);
                int limit = 100;
                int nc = 0;
                while (generator.next()) {
                    nc++;
                    out.println(generator.string() + "  " + generator.parenString());
                    if (nc >= limit)
                        break;
                }
                out.println(nc);
                out.println("===============");
            } catch (NoMinLenGrammarException e) {
                countNoMin++;
            }
            n++;
            count++;
            List<String> expectLines = new ArrayList<>();
            n = readExpectLines(lines, n, expectLines);
            n++;
        }
        out.println(count + " grammars, " + countNoMin + " exceptions");
    }

    static void testCount(List<String> lines) {
        int n = 0;
        int count = 0;
        int countFailed = 0;
        long start = System.nanoTime();
        while (n < lines.size()) {
            List<String> gramLines = new ArrayList<>();
            n = readGramLines(lines, n, gramLines);
            n++;
            count++;
            out.println(count);
            for (String line : gramLines)
                out.println(line);
            List<String> expectLines = new ArrayList<>();
            n = readExpectLines(lines, n, expectLines);
            n++;
            Grammar grammar = new Grammar(gramLines);
            int limit = 1000 * 1000;
            int limit2 = 1000 * 1000;
            for (int i = 0; i < expectLines.size(); i++) {
                String[] parts = expectLines.get(i).split(": ");
                int maxLen = Integer.parseInt(parts[0]);
                int expectedCount = Integer.parseInt(parts[1]);
                if (expectedCount > limit2)
                    break;
                Generator generator = new Generator(grammar, maxLen, RuleOrder.roShuffle);
                int nc = 0;
                while (generator.next()) {
                    nc++;
                    if (nc >= limit)
                        break;
                }
                if (nc != expectedCount) {
                    out.println("differ maxLen=" + maxLen + " expected=" + expectedCount + " found " + nc);
                    countFailed++;
                }
            }
        }
        out.println(count + " grammars, " + countFailed + " differ count");
        long duration = System.nanoTime() - start;
        out.println("duration=" + duration / 1e9);
    }

    static void testBad(List<String> lines) {
        int n = 0;
        int count = 0;
        int countNoMin = 0;
        while (n < lines.size()) {
            List<String> gramLines = new ArrayList<>();
            n = readGramLines(lines, n, gramLines);
            try {
                Grammar grammar = new Grammar(gramLines);
                out.println(count);
            } catch (NoMinLenGrammarException e) {
                countNoMin++;
            }
            n++;
            count++;
            List<String> expectLines = new ArrayList<>();
            n = readExpectLines(lines, n, expectLines);
            n++;
        }
        out.println(count + " grammars, " + countNoMin + " exceptions");
    }

    private static int readExpectLines(List<String> lines, int n, List<String> expectLines) {
        while (n < lines.size()) {
            String line = lines.get(n).trim();
            if (line.isEmpty()) break;
            expectLines.add(line);
            n++;
        }
        return n;
    }

    private static int readGramLines(List<String> lines, int n, List<String> gramLines) {
        while (n < lines.size()) {
            String line = lines.get(n).trim();
            if (line.isEmpty() || line.equals("---") || line.startsWith("===")) break;
            gramLines.add(line);
            n++;
        }
        return n;
    }

    static void testElimRecursion() {
        List<String> lines = new ArrayList<>();
        lines.add("E -> E + T");
        lines.add("E -> T");
        lines.add("T -> T * F");
        lines.add("T -> F");
        lines.add("F -> ( E )");
        lines.add("F -> i");
        Grammar grammar = new Grammar(lines);
        List<String> lines1 = grammar.toList();
        lines1.forEach(System.out::println);
        out.println();
        grammar.eliminationRecursion();
        List<String> lines2 = grammar.toList();
        lines2.forEach(System.out::println);
    }

    static void testElimIndirectRecursion() {
        List<String> lines = new ArrayList<>();
        lines.add("A -> B a");
        lines.add("A -> A a");
        lines.add("A -> c");
        lines.add("B -> B b");
        lines.add("B -> A b");
        lines.add("B -> d");
        Grammar grammar = new Grammar(lines);
        Generator generator = new Generator(grammar, 5, RuleOrder.roSort);
        while (generator.next())
            out.println(generator.string());
        List<String> lines1 = grammar.toList();
        lines1.forEach(out::println);
        out.println();
        grammar.eliminationRecursion();
        List<String> lines2 = grammar.toList();
        lines2.forEach(out::println);
        generator = new Generator(grammar, 5, RuleOrder.roSort);
        while (generator.next())
            out.println(generator.string());
    }

    static void testFactoring() {
        List<String> lines = new ArrayList<>();
        lines.add("S -> i E t S");
        lines.add("S -> i E t S e S");
        lines.add("S -> a");
        lines.add("E -> b");
        Grammar grammar = new Grammar(lines);
        ParsingTable table = new ParsingTable(grammar);
        for (int i = 1; i <= 4; i++) {
            System.out.printf("table LL(k) possibility for k=%d is %b %n", i, table.createLL(i));
            System.out.printf("need factorization for k=%d is %b %n", i, grammar.needsFactorization(i));
        }
    }

    public static void main(String[] args) {
        String currentDir = System.getProperty("user.dir");
        System.out.println("current dir: " + currentDir);
        try {
            labelGrammars();
        } catch (IOException e) {
            e.printStackTrace();
        }
     }
}
