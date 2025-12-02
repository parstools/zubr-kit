package parstools.zubr.grammar;

import parstools.zubr.graph.DG;
import parstools.zubr.graph.JohnsonsAlgorithm;
import parstools.zubr.graph.VertexEdge;
import parstools.zubr.util.Name;

import java.util.*;

public class Grammar implements Cloneable {
    public List<Nonterminal> nonterminals = new ArrayList<>();
    public List<Terminal> terminals = new ArrayList<>();
    static public Terminal EOF = new Terminal(null,"");
    Nonterminal startNt = null;
    public Set<Integer> cycleRules = new HashSet<>();

    private boolean transformed;
    public boolean transformed() {
        return transformed;
    }
    public boolean stayRecursion;

    boolean minLenOK = false;
    public boolean grammarOK() {
        return minLenOK && rulesDiffer();
    }

    private boolean rulesDiffer() {
        for (int i = 0; i < nonterminals.size(); i++) {
            Nonterminal nt = nonterminals.get(i);
            if (!nt.checkRulesDiffer())
                return false;
        }
        return true;
    }

    int directLeftRecursiveNt() {
        for (int i = 0; i < nonterminals.size(); i++) {
            Nonterminal nt = nonterminals.get(i);
            for (Rule rule : nt.rules)
                if (rule.directLeftRecursive(nt))
                    return i;
        }
        return -1;
    }

    void eliminationDirectRecursionForNt(Nonterminal nt) {
        List<Rule> recursiveRules = new ArrayList<>();
        List<Rule> nonrecursiveRules = new ArrayList<>();
        for (Rule rule : nt.rules) {
            if (rule.directLeftRecursive(nt)) {
                if (rule.size()>1) //without rules A->A
                    recursiveRules.add(rule);
            } else
                nonrecursiveRules.add(rule);
        }
        assert (!recursiveRules.isEmpty());
        Nonterminal newNt = insertNonterminal(nt);
        nt.rules.clear();
        for (Rule rule: nonrecursiveRules) {
            rule.add(newNt);
            nt.addRule(rule);
        }
        for (Rule rule: recursiveRules) {
            rule.remove(0);
            assert (!rule.isEmpty());
            rule.owner = newNt;
            rule.add(newNt);
            newNt.addRule(rule);
        }
        newNt.addRule(new Rule(nt.grammar, newNt));
        transformed = true;
    }

    private void substituteRules(Nonterminal owner, Nonterminal toSubstitute) {
        List<Rule> newRules = new ArrayList<>();
        for (Rule rule : owner.rules) {
            if (rule.startWithNonterminal(toSubstitute)) {
                for (Rule substRule : toSubstitute.rules) {
                    Rule newRule = substRule.clone(owner);
                    for (int k = 1; k < rule.size(); k++)
                        newRule.add(rule.get(k));
                    //eliminate ident rules: A->A
                    if (newRule.size()!=1 || newRule.getFirst().terminal || newRule.getFirst() != owner) {
                        newRule.index = newRules.size();
                        newRules.add(newRule);
                    }
                }
            } else {
                rule.index = newRules.size();
                newRules.add(rule);
            }
        }
        owner.rules = newRules;
    }

    private void substituteRules(RecurCycle  cycle) {
        assert(cycle.size() >= 2);
        substituteRules(cycle.get(0).owner, cycle.get(1).owner);
    }

    private Nonterminal insertNonterminal(Nonterminal owner) {
        Nonterminal newNt = new Nonterminal(this, newNameFrom(owner.name));
        newNt.rules = new ArrayList<>();
        nonterminals.add(owner.getIndex() + 1, newNt);
        newNt.computeMinLen();
        newNt.computeMaxLen();
        return newNt;
    }

    public Nonterminal findNt(String name) {
        for (Nonterminal nt: nonterminals)
            if (nt.name.equals(name))
                return nt;
        return null;
    }

    public Terminal findT(String name) {
        for (Terminal t: terminals)
            if (t.name.equals(name))
                return t;
        return null;
    }

    private String newNameFrom(String s) {
        int n;
        if (Name.hasNameSuffixNumber(s)) {
            n = Name.suffixNumber(s);
        } else n = 0;
        String nameAlone = Name.nameWithoutNumber(s);
        String newName = s;
        do {
            n++;
            newName = nameAlone + String.valueOf(n);
        } while (findNt(newName) != null);
        return newName;
    }

    void addNT(String ntName) {
        if (findNt(ntName) != null)
            return;
        Nonterminal nt = new Nonterminal(this, ntName);
        nonterminals.add(nt);
    }

    void addT(String tName) {
        if (findT(tName) != null)
            return;
        Terminal t = new Terminal(this, tName);
        terminals.add(t);
    }

    String parseNTname(String line) {
        int n = line.indexOf("->");
        if (n < 0)
            throw new RuntimeException("not found -> in grammar in line " + line);
        return line.substring(0, n).trim();
    }

    public Grammar() {
        //empty costructor for cloning
    }

    void setMinLen() {
        computeMinLen();
        minLenOK = checkMinLen();
        if (minLenOK) {
            computeNonNullableCount();
            detectCycles();
        }
    }

    public Grammar(List<String> lines) {
        for (String line : lines) {
            assert(!line.isEmpty());
            if (line.charAt(0) == ';') continue;
            addNT(parseNTname(line));
        }
        for (String line : lines) {
            if (line.charAt(0) == ';') continue;
            String ntName = parseNTname(line);
            int n = line.indexOf("->");
            String ruleString = line.substring(n + 2).trim();
            Nonterminal nt = findNt(ntName);
            Rule rule = new Rule(this, nt);
            rule.parse(ruleString);
            nt.addRule(rule);
        }
        setMinLen();
        computeMaxLen();
    }

    void createRandom(int seedBase) {
        Random randBase = new Random(seedBase);
        do {
            int seed = randBase.nextInt();
            nonterminals.clear();
            terminals.clear();
            Random random = new Random(seed);
            int ntCount = random.nextInt(3) + 1;
            for (int i = 0; i < ntCount; i++) {
                String name;
                if (i == 0)
                    name = "S";
                else {
                    char letterChar = (char) ('A' + i - 1);
                    name = String.valueOf(letterChar);
                }
                addNT(name);
            }
            int tCount = random.nextInt(3) + 2;
            for (int i = 0; i < ntCount; i++) {
                char letterChar = (char) ('a' + i);
                addT(String.valueOf(letterChar));
            }
            for (Nonterminal nt : nonterminals)
                nt.fillRandom(random);
            setMinLen();
            computeMaxLen();
        } while (!grammarOK());
    }

    private void computeNonNullableCount() {
        for (Nonterminal nt : nonterminals)
            for (Rule rule : nt.rules)
                rule.computeNonNullableCount();
    }

    private boolean checkMinLen() {
        for (Nonterminal nt : nonterminals) {
            if (nt.minLen < 0)
                return false;
            for (Rule rule : nt.rules)
                if (rule.minLen < 0)
                    return false;
        }
        return true;
    }

    private void computeMinLen() {
        boolean changed = true;
        while (changed) {
            changed = false;
            for (Nonterminal nt : nonterminals) {
                if (nt.computeMinLen())
                    changed = true;
            }
        }
    }

    private void computeMaxLen() {
        DG graph = new DG(nonterminals.size());
        for (Nonterminal nt : nonterminals) {
            int from = nt.getIndex();
            for (Rule rule : nt.rules)
                for (Symbol symbol : rule)
                    if (!symbol.terminal) {
                        int to = symbol.getIndex();
                        graph.addEdge(from, to, rule);
                    }
        }
        final int infinity = Integer.MAX_VALUE;
        for (int i=0; i<nonterminals.size(); i++)
            if (graph.detectCycle(i)) {
                Nonterminal nt = nonterminals.get(i);
                nt.isRecursive = true;
                nt.maxLen = infinity;
            }
        boolean changed = true;
        while (changed) {
            changed = false;
            for (Nonterminal nt : nonterminals) {
                if (nt.computeMaxLen())
                    changed = true;
            }
        }
    }

    public Symbol findSymbol(String name) {
        Symbol symbol = findNt(name);
        if (symbol == null)
            symbol = findT(name);
        return symbol;
    }

    public int getMinLen(Symbol symbol) {
        if (symbol.terminal)
            return 1;
        else
            return symbol.minLen;
    }

    public List<String> toList() {
        List<String> lines = new ArrayList<>();
        for (int i = 0; i < nonterminals.size(); i++) {
            Nonterminal nt = nonterminals.get(i);
            for (Rule rule : nt.rules)
                lines.add(rule.toString());
        }
        return lines;
    }

    public String[] toArray() {
        return toList().toArray(new String[0]);
    }

    public Object clone() {
        Grammar newGrammar = new Grammar();
        newGrammar.nonterminals = new ArrayList<>();
        for (Nonterminal nt : nonterminals) {
            Nonterminal ntCloned = (Nonterminal) nt.clone();
            ntCloned.grammar = newGrammar;
            newGrammar.nonterminals.add(ntCloned);
        }
        return newGrammar;
    }

    public Cycles cycles;

    void detectCycles() {
        DG graph = new DG(nonterminals.size());
        for (Nonterminal nt : nonterminals)
            for (Rule rule : nt.rules)
                if (rule.cycleSuspected()) {
                    for (Symbol symbol : rule) {
                        assert (!symbol.terminal);
                        graph.addEdge(nt.getIndex(), symbol.getIndex(), rule);
                    }
                }
        List<List<VertexEdge>> johnsonResult = JohnsonsAlgorithm.calculateCycles(graph);
        cycles = new Cycles(this, johnsonResult);
        for (Cycle c: cycles)
            for (Rule r: c) {
                int globKey = ruleToKey(r.owner, r);
                cycleRules.add(globKey);
            }
    }

    public static int ruleToKey(Symbol symbol, Rule rule) {
        return (symbol.getIndex() * 1000) + rule.index;
    }

    RecurCycles detectRecursion() {
        DG graph = new DG(nonterminals.size());
        for (Nonterminal nt : nonterminals)
            for (Rule rule : nt.rules)
                if (rule.startWithNonterminal()) {
                    Symbol symbol = rule.get(0);
                    graph.addEdge(nt.getIndex(), symbol.getIndex(), rule);
                }
        List<List<VertexEdge>> johnsonResult = JohnsonsAlgorithm.calculateCycles(graph);
        return new RecurCycles(this, johnsonResult);
    }

    void eliminationDirectRecursion() {
        boolean wasDirectRecursion;
        do {
            wasDirectRecursion = false;
            RecurCycles cycles = detectRecursion();
            for (RecurCycle cycle: cycles)
                if (cycle.size() == 1) {
                    eliminationDirectRecursionForNt(cycle.minOwner);
                    wasDirectRecursion = true;
                    break;
                }
        } while (wasDirectRecursion);
    }

    void eliminationIndirectRecursion() {
        boolean wasIndirectRecursion = false;
        RecurCycles cycles = detectRecursion();
        for (RecurCycle cycle: cycles) {
            if (cycle.size() < 2)
                throw new RuntimeException("must be not direct recursion");
            wasIndirectRecursion = true;
            transformed = true;
            substituteRules(cycle);
            break;
        }
        if (wasIndirectRecursion)
            eliminateUnreachedNonterminals();
    }

    private void eliminateUnreachedNonterminals() {
        List<Nonterminal> reachedList = new ArrayList<>();
        Set<Symbol> reachedSet = new HashSet<>();
        reachedList.add(nonterminals.getFirst());
        reachedSet.add(nonterminals.getFirst());
        int index = 0;
        while (index<reachedList.size()) {
            Nonterminal nt = reachedList.get(index);
            for (Rule rule : nt.rules)
                for (Symbol symbol: rule)
                    if (!symbol.terminal) {
                        if (!reachedSet.contains(symbol)) {
                            reachedList.add((Nonterminal) symbol);
                            reachedSet.add(symbol);
                        }
                    }
            index++;
        }
        nonterminals = reachedList;
    }

    public void eliminationRecursion() {
        int cnt = 0;
        while (true) {
            eliminationDirectRecursion();
            eliminationIndirectRecursion();
            RecurCycles cycles = detectRecursion();
            if (cycles.isEmpty()) break;
            cnt++;
            if (cnt >= 5) {
                stayRecursion = true;
                break;
            }
        }
        setMinLen();
        computeMaxLen();
    }

    public boolean needsFactorization(int k) {
        for (Nonterminal nt : nonterminals)
            if (nt.needsFactorization(k))
                return true;
        return false;
    }

    public void factorization(int k) {
        if (!needsFactorization(k))
            return;
        while (true) {
            boolean changed = false;
            List<Nonterminal> newNonTerminals = new ArrayList<>();
            for (Nonterminal nt : nonterminals) {
                Nonterminal newNt = nt.factorRules(k);
                newNonTerminals.add(nt);
                transformed = true;
                if (newNt != null) {
                    newNt.name = newNameFrom(nt.name);
                    newNt.computeMinLen();
                    newNt.computeMaxLen();
                    newNonTerminals.add(newNt);
                    changed = true;
                }
            }
            if (!changed)
                break;
            nonterminals = newNonTerminals;
        }
    }

    public Nonterminal addStartNt() {
        if (startNt!=null)
            return startNt;
        startNt = new Nonterminal(this, "");
        Rule startRule = new Rule(this, startNt);
        startRule.add(nonterminals.get(0));
        startNt.rules.add(startRule);
        return startNt;
    }

    public void addRule(Rule rule) {
        if (!nonterminals.contains(rule.owner))
            nonterminals.add(rule.owner);
        rule.owner.addRule(rule);
    }
}
