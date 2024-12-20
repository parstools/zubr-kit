package parstools.zubr.grammar;

public class Terminal extends Symbol{
    public Terminal(Grammar grammar, String name) {
        super(grammar, true, name);
        minLen = 1;
        maxLen = 1;
    }

    public int getIndex() {
        return grammar.terminals.indexOf(this);
    }
}
