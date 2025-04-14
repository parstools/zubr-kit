package parstools.zubr.lr;

import parstools.zubr.grammar.Symbol;

import java.util.ArrayList;
import java.util.List;

public class AbstractLR {
    private List<RowLR> rows = new ArrayList<>();
    RowLR row(int index) {
        return rows.get(index);
    }
    void add(int from, Symbol symbol, int to) {
        if (from >= rows.size()) {
            for (int i = rows.size(); i <= from; i++)
                rows.add(null);
            if (rows.get(from) == null)
                rows.set(from, new RowLR());
        }
    }
}
