package parstools.zubr.lr;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import parstools.zubr.grammar.Grammar;
import parstools.zubr.grammar.Symbol;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/** Reads the reference tables themselves; numbering is compared by graph isomorphism. */
class LRReferenceTablesTest {
    static Grammar expressions() {
        return new Grammar(List.of("E -> E + T", "E -> T", "T -> T * F",
                "T -> F", "F -> ( E )", "F -> id"));
    }

    static Grammar simple() {
        return new Grammar(List.of("E -> E + T", "E -> T", "T -> id"));
    }

    static Grammar assignment() {
        return new Grammar(List.of("S -> L = R", "S -> R", "L -> * R", "L -> id", "R -> L"));
    }

    static Grammar cc() {
        return new Grammar(List.of("S -> C C", "C -> c C", "C -> d"));
    }

    static Grammar notLalr() {
        return new Grammar(List.of("S -> a A d", "S -> b B d", "S -> a B e",
                "S -> b A e", "A -> c", "B -> c"));
    }

    record Table(List<String> columns, Map<String, List<String>> rows) {}

    private static List<String> cells(String line) {
        return Arrays.stream(line.substring(1, line.length() - 1).split("\\|"))
                .map(String::trim).toList();
    }

    private static List<Table> referenceTables() throws IOException {
        List<String> lines = Files.readAllLines(Path.of("docs/lr_parser_tables.md"));
        List<Table> tables = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            String header = lines.get(i);
            if (!header.startsWith("| state |") || !header.contains("| $ |")) continue;
            Map<String, List<String>> rows = new LinkedHashMap<>();
            i += 2;
            while (i < lines.size() && lines.get(i).startsWith("|")) {
                List<String> row = cells(lines.get(i++));
                rows.put(row.getFirst(), row);
            }
            tables.add(new Table(cells(header), rows));
        }
        assertEquals(10, tables.size(), "All reference ACTION/GOTO tables must be tested");
        return tables;
    }

    @TestFactory
    Stream<DynamicTest> allDocumentedTables() throws IOException {
        List<Table> tables = referenceTables();
        List<AbstractLR> parsers = List.of(
                new LR0(expressions()), new SLR(expressions()), new LR1(expressions()),
                new LR0(simple()), new SLR(assignment()), new LALR(assignment()),
                new LR1(cc()), new LALR(cc()), new LR1(notLalr()), new LALR(notLalr()));
        List<DynamicTest> tests = new ArrayList<>();
        for (int i = 0; i < tables.size(); i++) {
            int index = i;
            tests.add(DynamicTest.dynamicTest("Reference table " + i + " " + parsers.get(i).getClass().getSimpleName(),
                    () -> assertTable(tables.get(index), parsers.get(index))));
        }
        tests.add(DynamicTest.dynamicTest("Expression LALR equals reference SLR",
                () -> assertTable(tables.get(1), new LALR(expressions()))));
        return tests.stream();
    }

    private static String shiftTarget(String action) {
        return action.substring(1).replace("(", "").replace(")", "");
    }

    private static void bind(Map<String, Integer> mapping, String expected, int actual) {
        Integer previous = mapping.get(expected);
        if (previous != null) assertEquals(previous.intValue(), actual, "Transition to " + expected);
        else {
            assertFalse(mapping.containsValue(actual), "Two reference states mapped to " + actual);
            mapping.put(expected, actual);
        }
    }

    private static void assertTable(Table expected, AbstractLR parser) {
        assertEquals(expected.rows.size(), parser.stateCount());
        Map<String, Integer> mapping = new LinkedHashMap<>();
        mapping.put("0", 0);
        Set<String> visited = new HashSet<>();
        while (visited.size() < mapping.size()) {
            String label = mapping.keySet().stream().filter(s -> !visited.contains(s)).findFirst().orElseThrow();
            visited.add(label);
            State state = parser.states().get(mapping.get(label));
            List<String> reference = expected.rows.get(label);
            assertNotNull(reference, "Reference state " + label);
            for (int col = 1; col < expected.columns.size(); col++) {
                String name = expected.columns.get(col);
                String value = reference.get(col).replace("**", "");
                if (name.equals("$")) continue;
                Symbol symbol = parser.grammar().findSymbol(name);
                assertNotNull(symbol);
                String target = null;
                if (!symbol.terminal && !value.equals("—")) target = value;
                if (symbol.terminal)
                    for (String action : value.split("/"))
                        if (action.startsWith("s")) target = shiftTarget(action);
                Integer actual = state.transitions().get(symbol);
                if (target == null) assertNull(actual, label + " on " + name);
                else {
                    assertNotNull(actual, label + " on " + name);
                    bind(mapping, target, actual);
                }
            }
        }
        assertEquals(expected.rows.size(), mapping.size(), "Every state must be reachable");
        for (var entry : expected.rows.entrySet()) {
            RowLR row = parser.row(mapping.get(entry.getKey()));
            for (int col = 1; col < expected.columns.size(); col++) {
                String name = expected.columns.get(col);
                Symbol symbol = name.equals("$") ? null : parser.grammar().findSymbol(name);
                String value = entry.getValue().get(col).replace("**", "");
                String context = "State " + entry.getKey() + ", column " + name;
                if (symbol != null && !symbol.terminal) {
                    assertEquals(value.equals("—") ? null : mapping.get(value),
                            row.gotoState(symbol.getIndex()), context);
                } else {
                    Set<Action> actions = new HashSet<>();
                    if (!value.equals("—"))
                        for (String action : value.split("/")) {
                            if (action.equals("acc")) actions.add(Action.accept());
                            else if (action.startsWith("r"))
                                actions.add(Action.reduce(Integer.parseInt(action.substring(1))));
                            else actions.add(Action.shift(mapping.get(shiftTarget(action))));
                        }
                    assertEquals(actions, row.actions(symbol == null ? -1 : symbol.getIndex()), context);
                }
            }
        }
    }
}
