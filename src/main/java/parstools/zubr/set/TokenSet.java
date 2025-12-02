package parstools.zubr.set;

import parstools.zubr.grammar.Grammar;
import parstools.zubr.grammar.Symbol;

public class TokenSet {
    int maxLen;
    private final Tier[][] tiers;
    final int TIER_BUIlD = 0;
    final int TIER_DONE = 1;
    final int TIER_EOF = 2;
    Grammar grammar;

    public TokenSet(Grammar grammar, int maxLen) {
        this.grammar = grammar;
        this.maxLen = maxLen;
        tiers = new Tier[3][];
        for (int n = 0; n < 3; n++) {
            tiers[n] = new Tier[maxLen + 1];
            for (int i = 0; i <= maxLen; i++)
                tiers[n][i] = new Tier(grammar, i);
        }
    }

    //check if last result is well-formed
    boolean dbgNoBuild() {
        for (Tier tier: tiers[TIER_BUIlD])
            if (!tier.isEmpty())
                return false;
        return true;
    }

    boolean dbgNoShortDone() {
        for (Tier tier: tiers[TIER_DONE]) {
            if (tier.len == maxLen)
                continue;
            if (!tier.isEmpty())
                return false;
        }
        return true;
    }

    boolean dbgNoEmptyAndMaxEOF() {
        if (!tiers[TIER_EOF][0].isEmpty()) return false;
        if (!tiers[TIER_EOF][maxLen].isEmpty()) return false;
        return true;
    }

    public boolean dbgWellFormedLLSet() {
        return dbgNoBuild() && dbgNoShortDone() && dbgNoEmptyAndMaxEOF();
    }

    public int calculateSize() {
        int sum = 0;
        for (int n = 0; n < 3; n++)
            for (int i = 0; i < tiers[n].length; i++)
                sum += tiers[n][i].calculateSize();
        return sum;
    }

    public boolean isEmpty() {
        for (int n = 0; n < 3; n++)
            for (int i = 0; i <= maxLen; i++) {
                Tier tier = tiers[n][i];
                if (tier.trie != null)
                    return false;
            }
        return true;
    }

    public boolean isEmptyBuild() {
        for (int i = 0; i <= maxLen; i++) {
            Tier tier = tiers[TIER_BUIlD][i];
            if (tier.trie != null)
                return false;
        }
        return true;
    }

    public boolean isEmptyDone() {
        for (int i = 0; i <= maxLen; i++) {
            Tier tier = tiers[TIER_DONE][i];
            if (tier.trie != null)
                return false;
        }
        return true;
    }

    public boolean addSeqBuild(Sequence seq) {
        assert (seq.isEmpty() || seq.get(seq.size() - 1) >= 0);
        return tiers[TIER_BUIlD][seq.size()].addSeq(seq);
    }

    public boolean addSeqBuild(String str) {
        return addSeqBuild(new Sequence(grammar, str));
    }

    public boolean addSeqDone(Sequence seq) {
        return tiers[TIER_DONE][seq.size()].addSeq(seq);
    }

    public boolean addSeqDone(String str) {
        return addSeqDone(new Sequence(grammar, str));
    }

    public boolean addSeqEof(Sequence seq) {
        return tiers[TIER_EOF][seq.size()].addSeq(seq);
    }

    public boolean addSeqEof(String str) {
        return addSeqEof(new Sequence(grammar, str));
    }

    public boolean containsBuild(Sequence seq) {
        if (seq.size() >= maxLen)
            return false;
        return tiers[TIER_BUIlD][seq.size()].contains(seq);
    }

    public boolean containsBuild(String str) {
        return containsBuild(new Sequence(grammar, str));
    }

    public boolean containsDone(Sequence seq) {
        if (seq.size() > maxLen)
            return false;
        return tiers[TIER_DONE][seq.size()].contains(seq);
    }

    public boolean containsDone(String str) {
        return containsDone(new Sequence(grammar, str));
    }

    public boolean containsEof(Sequence seq) {
        if (seq.size() > maxLen)
            return false;
        return tiers[TIER_DONE][seq.size()].contains(seq);
    }

    public boolean containsEof(String str) {
        return containsDone(new Sequence(grammar, str));
    }

    public boolean contains(Sequence seq) {
        if (containsDone(seq))
            return true;
        else if (containsBuild(seq))
            return true;
        else if (containsEof(seq))
            return true;
        else
            return false;
    }

    public boolean contains(String str) {
        return contains(new Sequence(grammar, str));
    }

    public void addAllSeqDoneOrEof(SequenceSet sseq) {
        for (Sequence seq : sseq)
            if (seq.isEof())
                addSeqEof(seq);
            else
                addSeqDone(seq);
    }

    private String toStringPart(int n) {
        StringBuilder sb = new StringBuilder();
        boolean needSpace = false;
        for (int i = n == 2 ? 1 : 0; i <= maxLen; i++) {
            String tstr = tiers[n][i].toString();
            boolean empty = tstr.isEmpty();
            if (!empty) {
                if (needSpace) {
                    sb.append(" ");
                    needSpace = false;
                }
                sb.append(tstr);
                needSpace = true;
            }
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        String buildPart = toStringPart(TIER_BUIlD);
        if (!buildPart.isEmpty()) {
            sb.append("[");
            sb.append(buildPart);
            sb.append("]");
        }
        String donePart = toStringPart(TIER_DONE);
        String eofPart = toStringPart(TIER_EOF);
        if (!donePart.isEmpty() || !eofPart.isEmpty()) {
            sb.append("{");
            sb.append(donePart);
            if (!donePart.isEmpty() && !eofPart.isEmpty())
                sb.append(" ");
            sb.append(eofPart);
            sb.append("}");
        }
        if (buildPart.isEmpty() && donePart.isEmpty() && eofPart.isEmpty())
            return "{}";
        else
            return sb.toString();
    }

    public boolean addEpsilonBuild() {
        return tiers[TIER_BUIlD][0].addSeq(new Sequence(grammar));
    }

    public boolean addEpsilonDone() {
        return tiers[TIER_DONE][0].addSeq(new Sequence(grammar));
    }

    public void removeEpsilon() {
        tiers[TIER_DONE][0].clear();
    }

    public boolean hasEpsilon() {
        return tiers[TIER_DONE][0].trie != null;
    }

    private boolean addTier(int n, Tier tier) {
        return tiers[n][tier.len].unionWith(tier);
    }

    public boolean unionWith(TokenSet tokenSet) {
        boolean changed = false;
        for (int n = 0; n < 3; n++)
            for (Tier tier : tokenSet.tiers[n])
                if (addTier(n, tier))
                    changed = true;
        return changed;
    }

    public boolean unionWithoutEps(TokenSet tokenSet) {
        boolean changed = false;
        for (int n = 0; n < 3; n++)
            for (Tier tier : tokenSet.tiers[n])
                if (tier.len > 0)
                    if (addTier(n, tier))
                        changed = true;
        return changed;
    }

    public void replaceWith(TokenSet tokenSet) {
        boolean changed = false;
        for (int n = 0; n < 3; n++)
            for (int i = 0; i <= maxLen; i++)
                tiers[n][i] = tokenSet.tiers[n][i].clone();
    }

    public void appendStrings(Symbol symbol) {
        assert (symbol.terminal);
        assert (maxLen > 0);
        for (int i = maxLen; i >= 1; i--) {
            Trie trie = tiers[TIER_BUIlD][i - 1].trie;
            if (trie != null) {
                tiers[TIER_BUIlD][i - 1].trie = null;
                trie.appendStrings(symbol);
                int target;
                if (symbol.getIndex() == -1)
                    target = 2;
                else if (i == maxLen)
                    target = 1;
                else
                    target = 0;
                if (tiers[target][i].trie == null)
                    tiers[target][i].trie = trie;
                else
                    tiers[target][i].trie.unionWith(trie);
            }
        }
    }

    public boolean concatenable() {
        return isEmptyBuild();
    }

    void done() {
        for (int i = 0; i < maxLen; i++) {
            Tier t0 = tiers[TIER_BUIlD][i];
            Tier t1 = tiers[TIER_DONE][i];
            t1.unionWith(t0);
            t0.trie = null;
        }
    }


    public TokenSet clone() {
        TokenSet newSet = new TokenSet(grammar, maxLen);
        for (int n = 0; n < 3; n++)
            for (int i = 0; i <= maxLen; i++)
                newSet.tiers[n][i] = tiers[n][i].clone();
        return newSet;
    }

    public boolean isIntersecion(TokenSet second) {
        for (int n = 0; n < 3; n++)
            for (int i = 0; i <= maxLen; i++)
                if (tiers[n][i].isIntersection(second.tiers[n][i]))
                    return true;
        return false;
    }

    public TokenSet concat(TokenSet second) {
        assert (maxLen == second.maxLen);
        TokenSet result = new TokenSet(grammar, maxLen);
        assert (tiers[TIER_BUIlD][maxLen].isEmpty());
        assert (second.tiers[TIER_BUIlD][maxLen].isEmpty());
        for (int i = 0; i < maxLen; i++)
            for (int j = 0; j <= maxLen; j++) {
                int combinedLen = Math.min(i + j, maxLen);
                int target = combinedLen == maxLen ? TIER_DONE : TIER_BUIlD;
                Tier tier0 = tiers[TIER_BUIlD][i];
                Tier tier1 = second.tiers[TIER_DONE][j];
                Tier newTier = tier0.concat(tier1, combinedLen);
                result.tiers[target][combinedLen].unionWith(newTier);
            }
        for (int i = 0; i < maxLen; i++)
            for (int j = 0; j <= maxLen; j++) {
                int combinedLen = Math.min(i + j, maxLen);
                int target = combinedLen < maxLen ? TIER_EOF: TIER_DONE;
                Tier tier0 = tiers[TIER_BUIlD][i];
                Tier tier1 = second.tiers[TIER_EOF][j];
                Tier newTier = tier0.concat(tier1, combinedLen);
                result.tiers[target][combinedLen].unionWith(newTier);
            }
        for (int i = 0; i < maxLen; i++)
            for (int j = maxLen - i; j < maxLen; j++) {
                Tier tier0 = tiers[TIER_BUIlD][i];
                Tier tier1 = second.tiers[TIER_BUIlD][j];
                Tier newTier = tier0.concat(tier1, maxLen);
                result.tiers[TIER_DONE][maxLen].unionWith(newTier);
            }

        for (int i = 0; i <= maxLen; i++)
            result.tiers[TIER_DONE][i].unionWith(tiers[TIER_DONE][i]);
        result.tiers[TIER_DONE][maxLen].unionWith(tiers[TIER_BUIlD][maxLen]);
        return result;
    }

    public void rejectBuild() {
        for (int i = 0; i <= maxLen; i++)
            tiers[TIER_BUIlD][i].clear();
    }

    public void parse(String s) {
        if (s.length() < 2)
            return;
        String buildPart = "";
        String doneEofPart = "";
        if (s.charAt(0) == '[') {
            int pos = s.indexOf("]{");
            if (pos > 0) {
                buildPart = s.substring(1, pos);
                if (s.charAt(s.length() - 1) != '}')
                    throw new RuntimeException("bad parse string" + s);
                doneEofPart = s.substring(pos + 2, s.length() - 1);
            } else {
                if (s.charAt(s.length() - 1) != ']')
                    throw new RuntimeException("bad parse string" + s);
                buildPart = s.substring(1, s.length() - 1);
            }
        } else if (s.charAt(0) == '{') {
            doneEofPart = s.substring(1, s.length() - 1);
        } else throw new RuntimeException("bad parse string" + s);
        parseBuild(buildPart);
        parseDoneEof(doneEofPart);
    }

    private void parseBuild(String buildPart) {
        if (buildPart.isEmpty())
            return;
        String[] names = buildPart.split(" ");
        for (String name : names) {
            if (name.equals("eps"))
                addSeqBuild("");
            else
                addSeqBuild(name);
        }
    }

    private void parseDoneEof(String doneEofPart) {
        if (doneEofPart.isEmpty())
            return;
        String[] names = doneEofPart.split(" ");
        for (String name : names) {
            int pos = name.indexOf("$");
            if (pos >= 0) {
                if (pos != name.length() - 1)
                    throw new RuntimeException("bad token name " + name);
                addSeqEof(name);
            } else {
                if (name.equals("eps"))
                    addSeqDone("");
                else
                    addSeqDone(name);
            }
        }
    }

    public SequenceSet getPrefixes(int prefixLen) {
        SequenceSet ss = new SequenceSet();
        for (int i = prefixLen; i <= maxLen; i++)
            tiers[TIER_DONE][i].getPrefixes(prefixLen, ss);
        for (int i = 1; i <= maxLen; i++)
            tiers[TIER_EOF][i].getPrefixes(Math.min(i, prefixLen), ss);
        return ss;
    }

    public SingleTokenSet firstTokens() {
        SingleTokenSet sts = new SingleTokenSet(grammar);
        for (int i = 1; i <= 2; i++)
            for (int j = 1; j <= maxLen; j++)
                tiers[i][j].firstTokens(sts);
        return sts;
    }

    public SingleTokenSet nthTokens(Sequence seq) {
        SingleTokenSet sts = new SingleTokenSet(grammar);
        for (int i = 1; i <= 2; i++)
            for (int j = seq.size()+1; j <= maxLen; j++)
                tiers[i][j].nthTokens(seq, sts);
        return sts;
    }

    public SingleTokenSet nthTokens(String str) {
        return nthTokens(new Sequence(grammar, str));
    }
}
