# LR parser construction

The `parstools.zubr.lr` package builds LR(0), SLR(1), canonical LR(1),
and LALR(1) automata and ACTION/GOTO tables. It also provides a recognizer
for token lists. It does not yet build syntax trees or execute semantic actions.

## Usage

```java
import java.util.List;
import parstools.zubr.grammar.Grammar;
import parstools.zubr.lr.*;

Grammar grammar = new Grammar(List.of(
        "S -> C C",
        "C -> c C",
        "C -> d"));

LR1 canonical = new LR1(grammar);
LALR compact = new LALR(canonical);

assert canonical.stateCount() == 10;
assert compact.stateCount() == 7;
assert compact.isConflictFree();
assert compact.accepts(List.of("c", "d", "d"));

int terminal = grammar.findT("c").getIndex();
System.out.println(compact.row(0).actions(terminal));
System.out.println(compact.conflicts());
```

Construct `new LR0(grammar)` or `new SLR(grammar)` for the other variants.
Each grammar line represents one production. An empty right-hand side denotes
epsilon. Token names such as `id` are passed as whole strings, not individual
characters. EOF is implicit in `accepts` and uses index `-1` in ACTION keys.
An empty action set denotes a syntax error; a missing GOTO is returned as null.

Reduction numbers are global: production 0 is the synthetic start production,
followed by productions in nonterminal order and then each nonterminal's rule
order. Use `rule(number)` and `ruleNumber(rule)` to resolve them. These numbers
are separate from `Rule.index`, which is local to a nonterminal.

## Shared construction

- `ItemLR0` is a production/dot pair. `ItemLRk` adds an immutable terminal
  word; `ItemLR1` specializes it to one token.
- `State` supplies fixed-point closure and GOTO. State subclasses implement
  closure expansion and a state factory. Advancing an item preserves its
  lookahead and concrete type.
- `States` discovers the canonical collection breadth first. States are
  compared by their complete item sets, including lookaheads. Hashes only
  accelerate lookup; collisions do not merge unequal states.
- `AbstractLR` shares table construction, conflict reporting, and recognition.
  Every transition is retained, including transitions to existing states.
  Every completed production contributes reductions according to the variant.

| Variant | States | Reduction lookaheads |
|---|---|---|
| LR(0) | LR(0) closure and GOTO | Every terminal and EOF |
| SLR(1) | LR(0) closure and GOTO | FOLLOW of the production's left-hand side |
| LR(1) | Canonical LR(1) closure and GOTO | The completed item's lookahead |
| LALR(1) | Canonical LR(1) states merged by LR(0) core | Union of the merged items' lookaheads |

LR(1) closure expands `[A → α · B β, a]` using `FIRST(βa)`. The existing
`SetContainer.addFirstOfRule1` computes FIRST of the suffix. If that suffix
is nullable, closure also propagates the incoming lookahead. SLR uses the
existing `makeFirstSets1` and `makeFollowSets1` implementations.

LALR unions items in each group and remaps all transitions before building
the table. It preserves any conflicts introduced by merging. The canonical
parser passed to `new LALR(canonical)` remains unchanged.
`new LALR(lr0)` is a compatibility convenience that builds LR(1) from the
same grammar first; direct lookahead propagation on LR(0) states is not
implemented.

These constructions follow the standard
[LR(1)/LALR closure and merging algorithm from ECU](https://cs.ecu.edu/abrahamsonk/5220/spr16/Notes/Bottom-up/lr1.html)
and [LR/SLR table construction described by WPI](https://web.cs.wpi.edu/~kal/courses/compilers/module3/mybuparsing.html).

## Conflicts and grammar lifetime

An ACTION cell stores a set of actions. `conflicts()` reports all cells with
more than one action, and `Conflict` distinguishes shift/reduce and
reduce/reduce conflicts. Accept/reduce conflicts are also retained.
No precedence rule or default shift preference is applied.
`accepts` rejects conflicting tables with `IllegalStateException`;
for a conflict-free table it returns false for invalid input.

Build multiple parser variants from the same grammar as needed. Construction
uses the grammar's cached synthetic start production; it does not append an
extra nonterminal to its public nonterminal list or renumber its symbols.
Do not mutate the grammar after constructing a parser: items and reductions
retain the original production and symbol identities.

## Extension to k > 1

The common item representation stores lookahead words as immutable
`List<Integer>` values, and ACTION cells use the same word keys.
The closure/state factory and table reduction policy are separate from
canonical-state discovery. These are extension points for integrating the
existing FIRST(k)/FOLLOW(k) implementation later.

There is deliberately no LR(k) or LALR(k) construction yet. Their placeholder
constructors and `StatesLRk.createStates` throw
`UnsupportedOperationException` rather than returning incomplete tables.
Future support must add the appropriate closure and table algorithms, as well
as multi-token lookup in the recognizer; a longer key alone is not sufficient.

## Build and tests

Use a full JDK 21 or later and run `mvn test`. Maven Compiler targets Java 21
without preview features; Surefire 3.5.2 runs the JUnit 5 tests.
On a machine whose default Java points to an incomplete JDK installation:

```sh
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 mvn test
```

`LRReferenceTablesTest` reads all ten ACTION/GOTO tables directly from
[the reference document](lr_parser_tables.md) and compares every cell after
matching state numbers through transitions. It also checks that the expression
grammar's LALR table equals the documented SLR table.
`LRAlgorithmsTest` covers nullable productions and suffixes, EOF, recursion,
conflicts, recognition, repeated construction, immutable lookahead words,
and deliberate hash collisions.
