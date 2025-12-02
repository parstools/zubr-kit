package parstools.zubr.grammar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Nonterminal extends Symbol {
    @Override
    public int getIndex() {
        return grammar.nonterminals.indexOf(this);
    }
    public List<Rule> rules = new ArrayList<>();

    public Object clone() {
        Nonterminal newNt = new Nonterminal(grammar, name);
        newNt.rules = new ArrayList<>(rules);
        return newNt;
    }

    public int ruleCount() {
        return rules.size();
    }
    public boolean isRecursive = false;

    void addRule(Rule rule) {
        rule.owner = this;
        rule.index = rules.size();
        rules.add(rule);
    }

    public Nonterminal(Grammar grammar, String name) {
        super(grammar, false, name);
        this.grammar = grammar;
        minLen = -1;
        maxLen = -1;
    }

    boolean computeMinLen() {
        int old = minLen;
        boolean changed = false;
        for (Rule rule : rules) {
            if (rule.computeMinLen())
                changed = true;
        }
        for (Rule rule : rules) {
            if (rule.minLen >= 0) {
                if (minLen < 0)
                    minLen = rule.minLen;
                else
                    minLen = Math.min(minLen, rule.minLen);
            }
        }
        return minLen != old || changed;
    }

    boolean computeMaxLen() {
        final int infinity = Integer.MAX_VALUE;
        int old = maxLen;
        boolean changed = false;
        for (Rule rule : rules) {
            if (rule.computeMaxLen())
                changed = true;
        }
        for (Rule rule : rules) {
            if (rule.maxLen >= 0) {
                if (maxLen < 0)
                    maxLen = rule.maxLen;
                else
                    maxLen = Math.max(maxLen, rule.maxLen);
            }
        }
        return maxLen != old || changed;
    }

    public boolean needsFactorization(int k) {
        for (int i = 0; i < rules.size()-1; i++) {
            Rule rule0 = rules.get(i);
            for (int j = i + 1; j < rules.size(); j++) {
                Rule rule1 = rules.get(j);
                if (rule0.conflict(rule1, k))
                    return true;
            }
        }
        return false;
    }

    boolean checkRulesDiffer() {
        List<Rule> sortedRules = new ArrayList<>(rules);
        Collections.sort(sortedRules, (r1, r2) -> {
            int size1 = r1.size(), size2 = r2.size();
            int minSize = Math.min(size1, size2);
            for (int i = 0; i < minSize; i++) {
                int cmp = compare(r1.get(i), r2.get(i));
                if (cmp != 0) {
                    return cmp;
                }
            }
            return Integer.compare(size1, size2);
        });
        for (int i = 0; i < sortedRules.size()-1; i++) {
            Rule current = sortedRules.get(i);
            Rule next = sortedRules.get(i + 1);
            if (current.size() != next.size())
                continue;
            Rule commonPrefix = current.getCommonPrefix(next);
            if (commonPrefix.size() == current.size())
                return false;
        }
        return true;
    }

    public Nonterminal factorRules(int k) {
        List<Rule> sortedRules = new ArrayList<>(rules);
        Collections.sort(sortedRules, (r1, r2) -> {
            int size1 = r1.size(), size2 = r2.size();
            int minSize = Math.min(size1, size2);
            for (int i = 0; i < minSize; i++) {
                int cmp = compare(r1.get(i), r2.get(i));
                if (cmp != 0) {
                    return cmp;
                }
            }
            return Integer.compare(size1, size2);
        });
        Rule bestPrefix = new Rule(grammar, this);
        int bestLen = Integer.MAX_VALUE;
        for (int i = 0; i < sortedRules.size()-1; i++) {
            Rule current = sortedRules.get(i);
            Rule next = sortedRules.get(i+1);
            Rule commonPrefix = current.getCommonPrefix(next);
            int prefixLen = commonPrefix.size();
            if (commonPrefix.expandedLen(prefixLen) >= k) {
                if (prefixLen < bestLen || bestLen == Integer.MAX_VALUE) {
                    bestPrefix = commonPrefix;
                    bestLen = prefixLen;
                }
            }
        }
        if (bestPrefix.isEmpty())
            return null;
        Nonterminal newNt = new Nonterminal(grammar, "");
        List<Rule> newRules = new ArrayList<>();
        for (Rule rule: rules) {
            if (rule.startWith(bestPrefix)) {
                rule.subList(0, bestPrefix.size()).clear();
                newNt.addRule(rule);
            } else {
                newRules.add(rule);
            }
        }
        bestPrefix.add(newNt);
        newRules.add(0, bestPrefix);
        this.rules = newRules;
        return newNt;
    }

    public void fillRandom(Random random) {
        int ruleCount = random.nextInt(3) + 1;
        for (int i = 0; i < ruleCount ; i++) {
            Rule rule = new Rule(grammar,this);
            rule.fillRandom(random);
            addRule(rule);
        }
    }
}
