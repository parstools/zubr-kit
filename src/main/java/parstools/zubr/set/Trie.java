package parstools.zubr.set;

import parstools.zubr.grammar.Grammar;
import parstools.zubr.grammar.Symbol;

import java.util.*;

public class Trie {
    private TreeMap<Integer, Trie> map = new TreeMap<>();
    private int h;

    Trie(Grammar grammar, int h) {
        assert(h>=0);
        this.grammar = grammar;
        this.h = h;
    }

    Trie get(int key) {
        return map.get(key);
    }

    boolean containsKey(int key) {
        return map.containsKey(key);
    }

    void put(int key, Trie trie) {
        assert (trie == null || !trie.map.isEmpty() && h == trie.h + 1);
        map.put(key, trie);
    }

    Grammar grammar;

    boolean insertSuffix(Sequence seq) {
        assert (seq.size() >= h);
        if (seq.isEmpty())
            return false;// epsilon
        int h1 = 1;
        Trie trie = null;
        boolean modified = false;
        for (int i = seq.size() - 1; i >= seq.size() - h + 1; i--) {
            int token = seq.get(i);
            Trie newTrie = new Trie(grammar, h1);
            newTrie.put(token, trie);
            trie = newTrie;
            h1++;
        }
        int t = seq.get(seq.size() - h);
        if (!map.containsKey(t)) {
            put(t, trie);
            modified = true;
        }
        return modified;
    }

    Trie intersection(Trie trie1) {
        Set<Integer> set0 = map.keySet();
        Set<Integer> set1 = trie1.map.keySet();
        Set<Integer> intersection = new HashSet<>(set0);
        intersection.retainAll(set1);
        if (intersection.isEmpty())
            return null;
        Trie result = new Trie(grammar, h);
        assert(h >= 1 && h == trie1.h);
        if (h < 2) {
            for (Integer key : intersection)
                result.put(key, null);
            return result;
        } else {
            for (Integer key : intersection) {
                Trie sub0 = map.get(key);
                Trie sub1 = trie1.map.get(key);
                Trie sub = sub0.intersection(sub1);
                if (sub !=  null)
                    result.put(key, sub);
            }
            if (result.map.isEmpty())
                return null;
            else
                return result;
        }
    }

    boolean isIntersection(Trie trie1, int h) {
        Set<Integer> set0 = map.keySet();
        Set<Integer> set1 = trie1.map.keySet();
        Set<Integer> intersection = new HashSet<>(set0);
        intersection.retainAll(set1);
        if (intersection.isEmpty())
            return false;
        assert(h >= 1);
        if (h < 2)
            return true;
        else {
            for (Integer key : intersection) {
                Trie sub0 = map.get(key);
                Trie sub1 = trie1.map.get(key);
                Trie sub = sub0.intersection(sub1);
                if (sub !=  null)
                    return true;
            }
            return false;
        }
    }

    List<String> getSuffixes() {
        List<String> list = new ArrayList<>();
        for (Map.Entry<Integer, Trie> entry : map.entrySet()) {
            int t = entry.getKey();
            String tName;
            if (t == -1)
                tName = "$";
            else
                tName = grammar.terminals.get(t).name;
            Trie value = entry.getValue();
            List<String> subList = value != null? value.getSuffixes() : null;
            if (subList == null || subList.isEmpty())
                list.add(tName);
            else
                for (String s : subList) {
                    list.add(tName + s);
                }
        }
        return list;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        List<String> stringList = getSuffixes();
        for (int i = 0; i < stringList.size(); i++) {
            if (i > 0)
                sb.append(" ");
            sb.append(stringList.get(i));
        }
        return sb.toString();
    }

    public Trie clone() {
        Trie newTrie = new Trie(grammar, h);
        for (Map.Entry<Integer, Trie> entry : map.entrySet()) {
            int t = entry.getKey();
            Trie value = entry.getValue();
            if (value != null) {
                newTrie.put(t, value.clone());
            } else
                newTrie.put(t, null);
        }
        return newTrie;
    }

    Trie clonePrefix(int prefixLen) {
        assert(prefixLen >= 0);
        assert(prefixLen <= h);
        Trie newTrie = new Trie(grammar, prefixLen);
        if (prefixLen == 0)
            return newTrie;
        for (Map.Entry<Integer, Trie> entry : map.entrySet()) {
            int t = entry.getKey();
            Trie tr;
            if (prefixLen > 1) {
                Trie sub = entry.getValue();
                assert (sub != null);
                tr = sub.clonePrefix(prefixLen - 1);
            } else
                tr = null;
            newTrie.put(t, tr);
        }
        return newTrie;
    }

    public void appendStrings(int token) {
        if (map.isEmpty()) {
            assert(h == 0);
            put(token, null);
        } else if (h > 1) {
            for (Map.Entry<Integer, Trie> entry : map.entrySet()) {
                Trie sub = entry.getValue();
                assert(sub != null);
                sub.appendStrings(token);
            }
        } else {
            assert (h == 1);
            for (Map.Entry<Integer, Trie> entry : map.entrySet()) {
                Trie newTrie = new Trie(grammar, 1);
                newTrie.put(token, null);
                entry.setValue(newTrie);
            }
        }
        h++;
    }

    public void appendStrings(Symbol symbol) {
        assert (symbol.terminal);
        appendStrings(symbol.getIndex());
    }

    public int calculateSize() {
        assert (!map.isEmpty());
        int sum = 0;
        for (Map.Entry<Integer, Trie> entry : map.entrySet()) {
            Trie sub = entry.getValue();
            if (sub != null)
                sum += sub.calculateSize();
            else
                sum += 1;
        }
        return sum;
    }

    public boolean unionWith(Trie trie) {
        assert(trie != null);
        boolean modified = false;
        for (Map.Entry<Integer, Trie> entry : trie.map.entrySet()) {
            int t = entry.getKey();
            if (map.containsKey(t)) {
                Trie value = get(t);
                if (value != null && value.unionWith(entry.getValue()))
                    modified = true;
            } else {
                modified = true;
                Trie trieVal = entry.getValue();
                Trie val = trieVal == null ? null : trieVal.clone();
                put(t, val);
            }
        }
        return modified;
    }

    public Trie concatPrefixes(int prefixLen, Trie trie) {
        assert (prefixLen >= 0);
        if (prefixLen == 0) return clone();
        if (trie == null) return clone();
        prefixLen = Math.min(prefixLen, trie.h);
        Trie result = new Trie(grammar, h + prefixLen);
        for (Map.Entry<Integer, Trie> entry : map.entrySet()) {
            int t1 = entry.getKey();
            Trie sub = entry.getValue();
            assert (sub == null || !sub.map.isEmpty());
            Trie tr;
            if (sub == null)
                tr = trie.clonePrefix(prefixLen);
            else {
                tr = sub.concatPrefixes(prefixLen, trie);
            }
            result.put(t1, tr);
        }
        return result;
    }

    public void getPrefixes(Sequence seq, int prefixLen, SequenceSet ss) {
        for (Map.Entry<Integer, Trie> entry : map.entrySet()) {
            int t = entry.getKey();
            Sequence cloneSeq = seq.clone();
            cloneSeq.add(t);
            if (prefixLen == 1)
                ss.add(cloneSeq);
            else {
                Trie sub = entry.getValue();
                sub.getPrefixes(cloneSeq, prefixLen - 1, ss);
            }
        }
    }

    public void firstTokens(SingleTokenSet sts) {
        sts.addAll(map.keySet());
    }

    public void nthTokens(Sequence seq, SingleTokenSet sts) {
        Trie trie = findTrie(seq);
        if (trie != null)
            trie.firstTokens(sts);
    }

    private Trie findTrie(Sequence seq) {
        Trie tr = this;
        for (int t : seq) {
            tr = tr.get(t);
            if (tr == null)
                return null;
        }
        return tr;
    }

    public boolean contains(Sequence seq) {
        Trie tr = this;
        for (int i : seq) {
            if (tr.h>1) {
                tr = tr.get(i);
                if (tr == null)
                    return false;
            } else
                return tr.containsKey(i);
        }
        return true;
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }
}
