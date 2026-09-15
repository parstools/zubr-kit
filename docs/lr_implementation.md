# LR parser construction

The `parstools.zubr.lr` package builds LR(0), SLR(1), canonical LR(k),
and LALR(k) automata and ACTION/GOTO tables for k >= 1. It also provides a recognizer
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
| LR(k) | Canonical LR(k) closure and GOTO | The completed item's lookahead word |
| LALR(k) | Canonical LR(k) states merged by LR(0) core | Union of the merged items' lookahead words |

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

## LR(k) and sparse lookahead decisions

```java
Grammar grammar = new Grammar(List.of(
        "X -> Y",
        "X -> b Y a",
        "Y -> c",
        "Y -> c a"));

assert !new LRk(grammar, 1).isConflictFree();
LRk parser = new LRk(grammar, 2);
assert parser.isConflictFree();
assert parser.accepts(List.of("c"));
assert parser.accepts(List.of("c", "a"));
assert parser.accepts(List.of("b", "c", "a"));
assert parser.accepts(List.of("b", "c", "a", "a"));
assert !parser.accepts(List.of("b", "c"));
```

`new LRk(grammar, k)` requires a positive k. The generic k=1 construction
produces the same tables as `LR1`. An item contains an immutable lookahead word:

- k terminals without EOF; or
- zero to k-1 terminals followed by a single EOF marker.

EOF counts towards the maximum word length and is never repeated or padded.
For k=3, examples include `a b c`, `a b $`, `a $`, and `$`.
The synthetic initial item is `[S′ → · S, $]` for every k.

Closure expands `[A → α · B β, u]` with `[B → · γ, v]` for each
`v ∈ FIRST_k(βu)`. `StatesLRk` calls the existing
`SetContainer.makeFirstSetsK`, caches suffix sets computed by
`addFirstOfRuleK`, and concatenates them with each item's local context using
`TokenSet.concat`. Short unfinished suffixes remain in the BUILD tier until
that concatenation. Only actual words are extracted; global FOLLOW sets are
not substituted for local LR contexts.

For k>1, shift actions also need the complete lookahead context:
`[A → α · a β, u]` enables shift only on words in `FIRST_k(aβu)`.
The shift consumes one token, even though the decision inspects up to k.
For k=1 the existing terminal-transition rule is retained.
Reduction is entered on the completed item's word, and acceptance only on EOF.
For the example above, after `b c`, lookahead `a $` reduces `Y → c`,
whereas `a a` shifts. Enabling shift for every word starting with `a`
would introduce an incorrect conflict.

### Storage and lookup

`RowLR` stores ACTION as a trie using the existing `SortedIntMap` for
terminal edges. Common prefixes share nodes; missing words occupy no cells.
Construction never enumerates the alphabet's full Cartesian power.
The recognizer follows one edge per lookahead token and stops at k, EOF,
or a missing edge. It does not allocate a lookahead tuple at every decision.
It validates the entire relevant path rather than treating an incomplete
prefix as an action.

Use `row.actions(List.of(token1, token2))` for an exact word lookup, or
`row.actionRoot().next(token1).next(token2).actions()` to inspect a path.
A missing `next` returns null. `actions(int)` means an exact one-token key,
not all words beginning with that token. `actionEntryCount()` and
`actionNodeCount()` report populated cells and trie nodes (including the root).
`actions()` explicitly materializes a diagnostic map of populated cells;
normal parsing and conflict detection do not need that map.

The representation preserves correlations between tokens; it does not replace
words with independent token sets at each depth. Canonical states and used
lookahead sets can still grow exponentially in the worst case. Prefix sharing
avoids unused columns but does not eliminate that inherent cost. The FIRST(k)
sets retain their existing trie representation; canonical items enumerate the
lookahead words that actually occur.

### LALR(k)

`new LALRk(grammar, k)` or `new LALRk(canonicalLRk)` unions items in states
with equal LR(0) cores, remaps transitions, and constructs the sparse table
using the merged contexts. The canonical source is preserved. This first
implementation constructs LR(k) before merging, so its peak construction
memory still includes the canonical automaton.

LALR(k) for k>1 is a known construction; see
[Parr's dissertation, chapter 7, sections 7.4–7.5](https://www.antlr.org/papers/parr.phd.thesis.pdf).
As with LALR(1), merging can introduce conflicts. In particular, the example
above is LR(2) but this core-merged LALR(2) has shift/reduce on `a $`:
the contexts after `c` and `b c` have been combined.
Use `isConflictFree()` before recognizing input with the merged parser.

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

`LRkTest` covers the LR(2) example, a grammar requiring k=3, all EOF word
lengths, nullable suffixes, recursion, k=1 compatibility, merging, and trie
prefix sharing. A 63-terminal grammar at k=6 verifies that unused alphabet
combinations do not become columns.

`NaiveLRk` is a deliberately dense test-only implementation with independent
set-based FIRST, closure, state discovery, and recognition. It enumerates all
valid lookahead columns, including unused ones. `LRkOracleTest` compares its
item sets, transitions, every ACTION cell, and conflicts against the sparse
implementation for nine grammars at k=1,2,3. It also exhaustively compares short
inputs against both implementations and known finite/recursive languages.
