package parstools.zubr.set;

import parstools.zubr.generator.Generator;
import parstools.zubr.generator.RuleOrder;
import parstools.zubr.grammar.Grammar;
import parstools.zubr.grammar.Nonterminal;
import parstools.zubr.grammar.Rule;
import parstools.zubr.grammar.Symbol;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class SetContainer {
    Grammar grammar;
    public List<TokenSet> firstSets = new ArrayList<>();
    public List<TokenSet> followSets = new ArrayList<>();

    public int firstSize() {
        int result = 0;
        for (TokenSet tokenSet: firstSets)
            result += tokenSet.calculateSize();
        return result;
    }

    public int followSize() {
        int result = 0;
        for (TokenSet tokenSet: followSets)
            result += tokenSet.calculateSize();
        return result;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (TokenSet ts : firstSets)
            sb.append(ts.toString());
        sb.append(" | ");
        for (TokenSet ts : followSets)
            sb.append(ts.toString());
        sb.append("]");
        return sb.toString();
    }

    public SetContainer(Grammar grammar) {
        this.grammar = grammar;
    }

    public void reset(int k) {
        firstSets.clear();
        followSets.clear();
        for (int i = 0; i < grammar.nonterminals.size(); i++) {
            TokenSet firstSet = new TokenSet(grammar, k);
            firstSets.add(firstSet);
            TokenSet followSet = new TokenSet(grammar, k);
            followSets.add(followSet);
        }
    }

    public void computeSetsByGeneration(int k, int maxLen, int nextLimit) {
        Generator generator = new Generator(grammar, maxLen, RuleOrder.roShuffle);
        int counter = 0;
        while (generator.next()) {
            counter++;
            for (int i = 0; i < grammar.nonterminals.size(); i++) {
                SequenceSet sset = new SequenceSet();
                generator.collectFirst(i, k, sset);
                firstSets.get(i).addAllSeqDoneOrEof(sset);
                sset = new SequenceSet();
                generator.collectFollow(i, k, sset);
                followSets.get(i).addAllSeqDoneOrEof(sset);
            }
            if (counter >= nextLimit)
                break;
        }
    }

    public void computeSetsByRangeGeneration(int k, int maxLenStart, int maxLenEnd, int nextLimit) {
        for (int maxLen = maxLenStart; maxLen <= maxLenEnd; maxLen++)
            computeSetsByGeneration(k, maxLen, nextLimit);
    }

    public void readTest1(List<String> lines) {
        firstSets.clear();
        followSets.clear();
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            String[] parts = line.split("\t");
            assert (parts.length == 4);
            int nt = grammar.findSymbol(parts[0]).getIndex();
            boolean nullable = parts[1].equals("✔");
            TokenSet first = new TokenSet(grammar, 1);
            if (nullable) {
                Sequence seq = new Sequence(grammar, "");
                first.addSeqDone(seq);
            }

            String[] parts2 = parts[2].split(", ");
            for (String part : parts2) {
                Sequence seq = new Sequence(grammar, part);
                first.addSeqDone(seq);
            }
            TokenSet follow = new TokenSet(grammar, 1);
            String[] parts3 = parts[3].split(", ");
            for (String part : parts3) {
                Sequence seq = new Sequence(grammar, part);
                follow.addSeqDone(seq);
            }
            firstSets.add(first);
            followSets.add(follow);
        }
    }

    public void dump(PrintWriter printWriter) {
        printWriter.println("FIRST:");
        for (int i = 0; i < firstSets.size(); i++)
            printWriter.println(grammar.nonterminals.get(i).name + " " + firstSets.get(i).toString());
        printWriter.println("FOLLOW:");
        for (int i = 0; i < followSets.size(); i++)
            printWriter.println(grammar.nonterminals.get(i).name + " " + followSets.get(i).toString());
    }

    public boolean addFirstOfRule1(TokenSet outSet, Rule rule, int start) {
        boolean isEpsilon = true;
        boolean changed = false;
        for (int i = start; i < rule.size(); i++) {
            Symbol symbol = rule.get(i);
            if (symbol.terminal) {
                isEpsilon = false;
                Sequence seq = new Sequence(grammar, symbol);
                if (outSet.addSeqDone(seq))
                    changed = true;
                break;
            } else {
                TokenSet firstY = firstSetForSymbol(symbol);
                if (outSet.unionWithoutEps(firstY))
                    changed = true;
                if (!firstY.hasEpsilon()) {
                    isEpsilon = false;
                    break;
                }
            }
        }
        if (isEpsilon) {
            if (outSet.addEpsilonDone())
                changed = true;
        }
        return changed;
    }

    public boolean addFirstOfRuleK(TokenSet tempSet, int k, Rule rule, int start) {
        tempSet.addEpsilonBuild();
        for (int i = start; i < rule.size(); i++) {
            Symbol symbol = rule.get(i);
            if (symbol.terminal) {
                tempSet.appendStrings(symbol);
            } else {
                TokenSet firstY = firstSetForSymbol(symbol);
                TokenSet concated = tempSet.concat(firstY);
                if (concated.isEmpty())
                    return false;
                else {
                    tempSet.replaceWith(concated);
                }
            }
            if (tempSet.isEmptyBuild()) break;
        }
        return true;
    }

    TokenSet firstSetForIndex(int index) {
        return firstSets.get(index);
    }

    TokenSet followSetForIndex(int index) {
        return followSets.get(index);
    }

    TokenSet firstSetForSymbol(Symbol symbol) {
        assert (!symbol.terminal);
        return firstSetForIndex(symbol.getIndex());
    }

    TokenSet followSetForSymbol(Symbol symbol) {
        assert (!symbol.terminal);
        return followSetForIndex(symbol.getIndex());
    }

    public void makeFirstSets1() {
        boolean changed;
        do {
            changed = false;
            for (int i = grammar.nonterminals.size() - 1; i >= 0; i--) {
                Nonterminal X = grammar.nonterminals.get(i);
                for (int j = X.ruleCount() - 1; j >= 0; j--) {
                    Rule rule = X.rules.get(j);
                    boolean retChanged = addFirstOfRule1(firstSetForIndex(i), rule, 0);
                    if (retChanged) changed = true;
                }
            }
        } while (changed);
    }

    public void makeFirstSetsK(int k) {
        boolean changed;
        do {
            changed = false;
            for (int i = grammar.nonterminals.size() - 1; i >= 0; i--) {
                Nonterminal X = grammar.nonterminals.get(i);
                for (int j = X.ruleCount() - 1; j >= 0; j--) {
                    Rule rule = X.rules.get(j);
                    TokenSet tempSet = new TokenSet(grammar, k);
                    boolean canFinalize = addFirstOfRuleK(tempSet, k, rule, 0);
                    if (canFinalize)
                        tempSet.done();
                    if (firstSetForIndex(i).unionWith(tempSet))
                        changed = true;
                }
            }
        } while (changed);
        for (int i = 0; i < grammar.nonterminals.size(); i++)
            firstSetForIndex(i).rejectBuild();
    }

    public void makeFollowSets1() {
        followSetForIndex(0).addSeqEof(new Sequence(grammar, "$"));
        boolean changed;
        do {
            changed = false;
            for (int i = 0; i < grammar.nonterminals.size(); i++) {
                Nonterminal X = grammar.nonterminals.get(i);
                for (int j = 0; j < X.ruleCount(); j++) {
                    Rule rule = X.rules.get(j);
                    for (int k = 0; k < rule.size(); k++) {
                        Symbol symbol = rule.get(k);
                        if (!symbol.terminal) {
                            TokenSet tempSet = new TokenSet(grammar, 1);
                            addFirstOfRule1(tempSet, rule, k + 1);
                            boolean retChanged = followSetForSymbol(symbol).unionWithoutEps(tempSet);
                            if (retChanged) changed = true;
                            if (tempSet.hasEpsilon()) {
                                retChanged = followSetForSymbol(symbol).unionWithoutEps(followSetForIndex(i));
                                if (retChanged) changed = true;
                            }
                        }
                    }
                }
            }
        } while (changed);
    }

    public void makeFollowSetsK(int k) {
        Sequence eofSeq = new Sequence(grammar, "$");
        if (k == 1)
            followSetForIndex(0).addSeqDone(eofSeq);
        else
            followSetForIndex(0).addSeqEof(eofSeq);
        boolean changed;
        do {
            changed = false;
            for (int i = 0; i < grammar.nonterminals.size(); i++) {
                Nonterminal X = grammar.nonterminals.get(i);
                for (int j = 0; j < X.ruleCount(); j++) {
                    Rule rule = X.rules.get(j);
                    for (int n = 0; n < rule.size(); n++) {
                        Symbol symbol = rule.get(n);
                        if (!symbol.terminal) {
                            TokenSet tempSet = new TokenSet(grammar, k);
                            boolean canFinalize = addFirstOfRuleK(tempSet, k, rule, n+1);
                            if (canFinalize) {
                                if (!tempSet.isEmptyBuild()) {
                                    TokenSet concated = tempSet.concat(followSetForIndex(i));
                                    if (!concated.isEmpty()) {
                                        tempSet.replaceWith(concated);
                                        tempSet.done();
                                    }
                                }
                                else tempSet.done();
                            }
                            if (followSetForIndex(symbol.getIndex()).unionWith(tempSet))
                                changed = true;
                        }
                    }
                }
            }
        } while (changed);
    }
}
