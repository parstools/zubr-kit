# LR Parser Tables: Different Grammars and Conflicts

## Conventions

Augmented grammar and reduction numbering:

```text
0. S′ → E
1. E  → E + T
2. E  → T
3. T  → T * F
4. T  → F
5. F  → ( E )
6. F  → id
```

`sN` denotes a *shift* and a transition to state `N`, `rN` denotes a
reduction by production `N`, `acc` denotes acceptance, and `—` denotes no
action. The `E`, `T`, and `F` columns form the `GOTO` part; the remaining
columns form the `ACTION` part. The state numbering is one valid numbering of
the canonical item sets.

The key result is that this grammar **is not LR(0)**, but **is SLR(1), LALR(1),
and LR(1)**. The SLR and LALR tables have 12 states. Canonical LR(1) has 22
states because it distinguishes lookahead contexts.

Source for the grammar, item sets, and SLR table construction: [WPI,
*Bottom-Up Parsing*](https://web.cs.wpi.edu/~kal/courses/compilers/module3/mybuparsing.html).

### FIRST and FOLLOW

| nonterminal | FIRST | FOLLOW |
|---|---|---|
| `E` | `{ (, id }` | `{ +, ), $ }` |
| `T` | `{ (, id }` | `{ +, *, ), $ }` |
| `F` | `{ (, id }` | `{ +, *, ), $ }` |

### Canonical LR(0) Item Sets

| state | items |
|---:|---|
| 0 | `S′ → · E`; `E → · E + T`; `E → · T`; `T → · T * F`; `T → · F`; `F → · ( E )`; `F → · id` |
| 1 | `S′ → E ·`; `E → E · + T` |
| 2 | `E → T ·`; `T → T · * F` |
| 3 | `T → F ·` |
| 4 | `F → ( · E )`; `E → · E + T`; `E → · T`; `T → · T * F`; `T → · F`; `F → · ( E )`; `F → · id` |
| 5 | `F → id ·` |
| 6 | `E → E + · T`; `T → · T * F`; `T → · F`; `F → · ( E )`; `F → · id` |
| 7 | `T → T * · F`; `F → · ( E )`; `F → · id` |
| 8 | `F → ( E · )`; `E → E · + T` |
| 9 | `E → E + T ·`; `T → T · * F` |
| 10 | `T → T * F ·` |
| 11 | `F → ( E ) ·` |

The most important transitions are `GOTO(0,E)=1`, `GOTO(0,T)=2`,
`GOTO(2,*)=7`, `GOTO(6,T)=9`, and `GOTO(9,*)=7`. The last two states contain
both a completed item and the possibility of shifting `*`, which explains the
LR(0) conflicts.

## LR(0): Table with Conflicts

| state | id | + | * | ( | ) | $ | E | T | F |
|---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|
| 0 | s5 | — | — | s4 | — | — | 1 | 2 | 3 |
| 1 | — | s6 | — | — | — | acc | — | — | — |
| 2 | r2 | r2 | **s7/r2** | r2 | r2 | r2 | — | — | — |
| 3 | r4 | r4 | r4 | r4 | r4 | r4 | — | — | — |
| 4 | s5 | — | — | s4 | — | — | 8 | 2 | 3 |
| 5 | r6 | r6 | r6 | r6 | r6 | r6 | — | — | — |
| 6 | s5 | — | — | s4 | — | — | — | 9 | 3 |
| 7 | s5 | — | — | s4 | — | — | — | — | 10 |
| 8 | — | s6 | — | — | s11 | — | — | — | — |
| 9 | r1 | r1 | **s7/r1** | r1 | r1 | r1 | — | — | — |
| 10 | r3 | r3 | r3 | r3 | r3 | r3 | — | — | — |
| 11 | r5 | r5 | r5 | r5 | r5 | r5 | — | — | — |

The shift/reduce conflicts occur in cells `(2, *)` and `(9, *)`. The problem
is the LR(0) rule: a completed item such as `E → T ·` mandates a reduction
**without examining the next token**. The same state, however, contains
`T → T · * F`, which mandates a shift for `*`.

## SLR(1): Conflict-Free Table

In SLR, reductions are entered only for symbols in the `FOLLOW` set of the
left-hand side:

```text
FOLLOW(E) = { +, ), $ }
FOLLOW(T) = { +, *, ), $ }
FOLLOW(F) = { +, *, ), $ }
```

| state | id | + | * | ( | ) | $ | E | T | F |
|---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|
| 0 | s5 | — | — | s4 | — | — | 1 | 2 | 3 |
| 1 | — | s6 | — | — | — | acc | — | — | — |
| 2 | — | r2 | s7 | — | r2 | r2 | — | — | — |
| 3 | — | r4 | r4 | — | r4 | r4 | — | — | — |
| 4 | s5 | — | — | s4 | — | — | 8 | 2 | 3 |
| 5 | — | r6 | r6 | — | r6 | r6 | — | — | — |
| 6 | s5 | — | — | s4 | — | — | — | 9 | 3 |
| 7 | s5 | — | — | s4 | — | — | — | — | 10 |
| 8 | — | s6 | — | — | s11 | — | — | — | — |
| 9 | — | r1 | s7 | — | r1 | r1 | — | — | — |
| 10 | — | r3 | r3 | — | r3 | r3 | — | — | — |
| 11 | — | r5 | r5 | — | r5 | r5 | — | — | — |

In particular, the `E → T` reduction is not entered in the `*` column because
`* ∉ FOLLOW(E)`. This removes both LR(0) conflicts.

## LALR(1): Conflict-Free Table

For this grammar, the LALR(1) `ACTION/GOTO` table is **identical to the SLR(1)
table above** (with the same state numbering). This is not a general rule:
LALR uses more precise lookaheads than the global `FOLLOW` sets, but merging
states with the same LR(0) core produces the same actions here.

## Canonical LR(1): Conflict-Free Table

The complete LR(1) table appears below. Split states such as `1` and `7` have
the same `T → F ·` core but different permitted lookaheads.

| state | id | + | * | ( | ) | $ | E | T | F |
|---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|
| 0 | s4 | — | — | s3 | — | — | 5 | 2 | 1 |
| 1 | — | r4 | r4 | — | — | r4 | — | — | — |
| 2 | — | r2 | s6 | — | — | r2 | — | — | — |
| 3 | s10 | — | — | s9 | — | — | 11 | 8 | 7 |
| 4 | — | r6 | r6 | — | — | r6 | — | — | — |
| 5 | — | s12 | — | — | — | acc | — | — | — |
| 6 | s4 | — | — | s3 | — | — | — | — | 13 |
| 7 | — | r4 | r4 | — | r4 | — | — | — | — |
| 8 | — | r2 | s14 | — | r2 | — | — | — | — |
| 9 | s10 | — | — | s9 | — | — | 15 | 8 | 7 |
| 10 | — | r6 | r6 | — | r6 | — | — | — | — |
| 11 | — | s16 | — | — | s17 | — | — | — | — |
| 12 | s4 | — | — | s3 | — | — | — | 18 | 1 |
| 13 | — | r3 | r3 | — | — | r3 | — | — | — |
| 14 | s10 | — | — | s9 | — | — | — | — | 19 |
| 15 | — | s16 | — | — | s20 | — | — | — | — |
| 16 | s10 | — | — | s9 | — | — | — | 21 | 7 |
| 17 | — | r5 | r5 | — | — | r5 | — | — | — |
| 18 | — | r1 | s6 | — | — | r1 | — | — | — |
| 19 | — | r3 | r3 | — | r3 | — | — | — | — |
| 20 | — | r5 | r5 | — | r5 | — | — | — | — |
| 21 | — | r1 | s14 | — | r1 | — | — | — | — |

## Example 2: An LR(0) Grammar Without Conflicts

Source for the grammar and canonical item sets: [Stony Brook University,
*LR Parsing*](https://www3.cs.stonybrook.edu/~cram/cse504/Spring16/Lectures/lrparser-handout.pdf),
slides 17–18.

```text
0. E′ → E
1. E  → E + T
2. E  → T
3. T  → id
```

### FIRST and FOLLOW

| nonterminal | FIRST | FOLLOW |
|---|---|---|
| `E` | `{ id }` | `{ +, $ }` |
| `T` | `{ id }` | `{ +, $ }` |

### LR(0) Item Sets

| state | definition | items |
|---:|---|---|
| 0 | `CLOSURE({E′ → · E})` | `E′ → · E`; `E → · E + T`; `E → · T`; `T → · id` |
| 1 | `GOTO(0,E)` | `E′ → E ·`; `E → E · + T` |
| 2 | `GOTO(0,T)` | `E → T ·` |
| 3 | `GOTO(0,id)` | `T → id ·` |
| 4 | `GOTO(1,+)` | `E → E + · T`; `T → · id` |
| 5 | `GOTO(4,T)` | `E → E + T ·` |

### LR(0) Table

| state | id | + | $ | E | T |
|---:|:---:|:---:|:---:|:---:|:---:|
| 0 | s3 | — | — | 1 | 2 |
| 1 | — | s4 | acc | — | — |
| 2 | r2 | r2 | r2 | — | — |
| 3 | r3 | r3 | r3 | — | — |
| 4 | s3 | — | — | — | 5 |
| 5 | r1 | r1 | r1 | — | — |

No cell contains more than one action, so the grammar is LR(0). It is
therefore also SLR(1), LALR(1), and LR(1).

## Example 3: LALR(1), but Not SLR(1)

Source for the grammar, FIRST/FOLLOW sets, LR(0) sets, and conflict analysis:
[East Carolina University, *LALR(1) parsers*](https://cs.ecu.edu/abrahamsonk/5220/spr16/Notes/Bottom-up/lalr.html).
The complete LR(1) table for the same example also appears in the [University
of San Francisco slides](https://www.cs.usfca.edu/~galles/cs414/lecture/lecture5.java.printable.pdf).

```text
0. S′ → S
1. S  → L = R
2. S  → R
3. L  → * R
4. L  → id
5. R  → L
```

### FIRST and FOLLOW

| nonterminal | FIRST | FOLLOW |
|---|---|---|
| `S` | `{ *, id }` | `{ $ }` |
| `L` | `{ *, id }` | `{ =, $ }` |
| `R` | `{ *, id }` | `{ =, $ }` |

### LR(0) Item Sets Used by SLR and LALR

| state | items |
|---:|---|
| 0 | `S′ → · S`; `S → · L = R`; `S → · R`; `L → · * R`; `L → · id`; `R → · L` |
| 1 | `S′ → S ·` |
| 2 | `S → L · = R`; `R → L ·` |
| 3 | `S → R ·` |
| 4 | `L → * · R`; `R → · L`; `L → · * R`; `L → · id` |
| 5 | `L → id ·` |
| 6 | `S → L = · R`; `R → · L`; `L → · * R`; `L → · id` |
| 7 | `L → * R ·` |
| 8 | `R → L ·` |
| 9 | `S → L = R ·` |

### SLR(1) Table: Conflict

| state | id | * | = | $ | S | L | R |
|---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|
| 0 | s5 | s4 | — | — | 1 | 2 | 3 |
| 1 | — | — | — | acc | — | — | — |
| 2 | — | — | **s6/r5** | r5 | — | — | — |
| 3 | — | — | — | r2 | — | — | — |
| 4 | s5 | s4 | — | — | — | 8 | 7 |
| 5 | — | — | r4 | r4 | — | — | — |
| 6 | s5 | s4 | — | — | — | 8 | 9 |
| 7 | — | — | r3 | r3 | — | — | — |
| 8 | — | — | r5 | r5 | — | — | — |
| 9 | — | — | — | r1 | — | — | — |

State 2 contains `S → L · = R`, so `=` mandates `s6`. At the same time, it
contains `R → L ·`, and SLR enters `r5` for all of `FOLLOW(R)={=,$}`. This
causes the `s6/r5` conflict.

### LALR(1) Table: No Conflict

| state | id | * | = | $ | S | L | R |
|---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|
| 0 | s5 | s4 | — | — | 1 | 2 | 3 |
| 1 | — | — | — | acc | — | — | — |
| 2 | — | — | s6 | r5 | — | — | — |
| 3 | — | — | — | r2 | — | — | — |
| 4 | s5 | s4 | — | — | — | 8 | 7 |
| 5 | — | — | r4 | r4 | — | — | — |
| 6 | s5 | s4 | — | — | — | 8 | 9 |
| 7 | — | — | r3 | r3 | — | — | — |
| 8 | — | — | r5 | r5 | — | — | — |
| 9 | — | — | — | r1 | — | — | — |

The local lookahead of the `R → L ·` item in state 2 is only `$`. LALR
therefore does not enter `r5` under `=`, leaving the unambiguous `s6` action.

## Example 4: LR(1) and Merging into LALR(1)

The source provides the complete states and both tables: [East Carolina
University, *Canonical LR(1) Parsers*](https://cs.ecu.edu/abrahamsonk/5220/spr16/Notes/Bottom-up/lr1.html).

```text
0. S′ → S
1. S  → C C
2. C  → c C
3. C  → d
```

### FIRST and FOLLOW

| nonterminal | FIRST | FOLLOW |
|---|---|---|
| `S` | `{ c, d }` | `{ $ }` |
| `C` | `{ c, d }` | `{ c, d, $ }` |

### Canonical LR(1) Item Sets

The notation `c/d` represents two items with the same core and lookaheads `c`
and `d`.

| state | LR(1) items |
|---:|---|
| 0 | `[S′ → · S,$]`; `[S → · C C,$]`; `[C → · c C,c/d]`; `[C → · d,c/d]` |
| 1 | `[S′ → S ·,$]` |
| 2 | `[S → C · C,$]`; `[C → · c C,$]`; `[C → · d,$]` |
| 3 | `[C → c · C,c/d]`; `[C → · c C,c/d]`; `[C → · d,c/d]` |
| 4 | `[C → d ·,c/d]` |
| 5 | `[S → C C ·,$]` |
| 6 | `[C → c · C,$]`; `[C → · c C,$]`; `[C → · d,$]` |
| 7 | `[C → d ·,$]` |
| 8 | `[C → c C ·,c/d]` |
| 9 | `[C → c C ·,$]` |

Transitions: `0—S→1`, `0—C→2`, `0—c→3`, `0—d→4`, `2—C→5`,
`2—c→6`, `2—d→7`, `3—C→8`, `3—c→3`, `3—d→4`, `6—C→9`,
`6—c→6`, `6—d→7`.

### Canonical LR(1) Table

| state | c | d | $ | S | C |
|---:|:---:|:---:|:---:|:---:|:---:|
| 0 | s3 | s4 | — | 1 | 2 |
| 1 | — | — | acc | — | — |
| 2 | s6 | s7 | — | — | 5 |
| 3 | s3 | s4 | — | — | 8 |
| 4 | r3 | r3 | — | — | — |
| 5 | — | — | r1 | — | — |
| 6 | s6 | s7 | — | — | 9 |
| 7 | — | — | r3 | — | — |
| 8 | r2 | r2 | — | — | — |
| 9 | — | — | r2 | — | — |

### Sets and Table After Merging into LALR(1)

States with identical LR(0) cores are merged: `3+6`, `4+7`, and `8+9`.

| LALR state | items after merging |
|---:|---|
| 0 | `[S′ → · S,$]`; `[S → · C C,$]`; `[C → · c C,c/d]`; `[C → · d,c/d]` |
| 1 | `[S′ → S ·,$]` |
| 2 | `[S → C · C,$]`; `[C → · c C,$]`; `[C → · d,$]` |
| 3+6 | `[C → c · C,c/d/$]`; `[C → · c C,c/d/$]`; `[C → · d,c/d/$]` |
| 4+7 | `[C → d ·,c/d/$]` |
| 5 | `[S → C C ·,$]` |
| 8+9 | `[C → c C ·,c/d/$]` |

| state | c | d | $ | S | C |
|---:|:---:|:---:|:---:|:---:|:---:|
| 0 | s(3+6) | s(4+7) | — | 1 | 2 |
| 1 | — | — | acc | — | — |
| 2 | s(3+6) | s(4+7) | — | — | 5 |
| 3+6 | s(3+6) | s(4+7) | — | — | 8+9 |
| 4+7 | r3 | r3 | r3 | — | — |
| 5 | — | — | r1 | — | — |
| 8+9 | r2 | r2 | r2 | — | — |

Merging reduces the number of states from 10 to 7 and introduces no conflict.

## Example 5: LR(1), but Not LALR(1)

Source for the grammar and the explanation of the conflict after merging:
[Stack Overflow, *Example of grammar that works in LR(1) but not LALR(1)?*](https://stackoverflow.com/questions/54394438/example-of-grammar-that-works-in-lr1-but-not-lalr1).
This is Example 4.58 from the second edition of the so-called Dragon Book;
the [UAF material](https://www.cs.uaf.edu/~cs631/notes/parse/node5.html)
also identifies this example as a reduce/reduce conflict caused by LALR
merging.

```text
0. S′ → S
1. S  → a A d
2. S  → b B d
3. S  → a B e
4. S  → b A e
5. A  → c
6. B  → c
```

### FIRST and FOLLOW

| nonterminal | FIRST | FOLLOW |
|---|---|---|
| `S` | `{ a, b }` | `{ $ }` |
| `A` | `{ c }` | `{ d, e }` |
| `B` | `{ c }` | `{ d, e }` |

### Canonical LR(1) Item Sets

| state | LR(1) items |
|---:|---|
| 0 | `[S′ → · S,$]`; `[S → · a A d,$]`; `[S → · b B d,$]`; `[S → · a B e,$]`; `[S → · b A e,$]` |
| 1 | `[S′ → S ·,$]` |
| 2 | `[S → a · A d,$]`; `[S → a · B e,$]`; `[A → · c,d]`; `[B → · c,e]` |
| 3 | `[S → b · B d,$]`; `[S → b · A e,$]`; `[A → · c,e]`; `[B → · c,d]` |
| 4 | `[S → a A · d,$]` |
| 5 | `[S → a B · e,$]` |
| 6 | `[A → c ·,d]`; `[B → c ·,e]` |
| 7 | `[S → b A · e,$]` |
| 8 | `[S → b B · d,$]` |
| 9 | `[A → c ·,e]`; `[B → c ·,d]` |
| 10 | `[S → a A d ·,$]` |
| 11 | `[S → a B e ·,$]` |
| 12 | `[S → b A e ·,$]` |
| 13 | `[S → b B d ·,$]` |

### Canonical LR(1) Table: No Conflict

| state | a | b | c | d | e | $ | S | A | B |
|---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|
| 0 | s2 | s3 | — | — | — | — | 1 | — | — |
| 1 | — | — | — | — | — | acc | — | — | — |
| 2 | — | — | s6 | — | — | — | — | 4 | 5 |
| 3 | — | — | s9 | — | — | — | — | 7 | 8 |
| 4 | — | — | — | s10 | — | — | — | — | — |
| 5 | — | — | — | — | s11 | — | — | — | — |
| 6 | — | — | — | r5 | r6 | — | — | — | — |
| 7 | — | — | — | — | s12 | — | — | — | — |
| 8 | — | — | — | s13 | — | — | — | — | — |
| 9 | — | — | — | r6 | r5 | — | — | — | — |
| 10 | — | — | — | — | — | r1 | — | — | — |
| 11 | — | — | — | — | — | r3 | — | — | — |
| 12 | — | — | — | — | — | r4 | — | — | — |
| 13 | — | — | — | — | — | r2 | — | — | — |

### LALR(1) Table: Conflict After Merging

States 6 and 9 have the same LR(0) core, `{A → c ·, B → c ·}`. After
combining the lookaheads, we obtain `[A → c ·,d/e]` and `[B → c ·,d/e]`.

| state | a | b | c | d | e | $ | S | A | B |
|---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|
| 0 | s2 | s3 | — | — | — | — | 1 | — | — |
| 1 | — | — | — | — | — | acc | — | — | — |
| 2 | — | — | s(6+9) | — | — | — | — | 4 | 5 |
| 3 | — | — | s(6+9) | — | — | — | — | 7 | 8 |
| 4 | — | — | — | s10 | — | — | — | — | — |
| 5 | — | — | — | — | s11 | — | — | — | — |
| 6+9 | — | — | — | **r5/r6** | **r5/r6** | — | — | — | — |
| 7 | — | — | — | — | s12 | — | — | — | — |
| 8 | — | — | — | s13 | — | — | — | — | — |
| 10 | — | — | — | — | — | r1 | — | — | — |
| 11 | — | — | — | — | — | r3 | — | — | — |
| 12 | — | — | — | — | — | r4 | — | — | — |
| 13 | — | — | — | — | — | r2 | — | — | — |

These are reduce/reduce conflicts. Canonical LR(1) keeps states 6 and 9
separate, so it knows whether to reduce `c` to `A` or to `B`.

## Examples of Class Boundaries and Conflicts

| Grammar (always add `S′ → S`) | LR(0) | SLR(1) | LALR(1) | LR(1) | What happens in the table |
|---|:---:|:---:|:---:|:---:|---|
| The expression grammar above | no | yes | yes | yes | LR(0): shift/reduce on `*`; SLR restricts the reduction using `FOLLOW`. |
| `S → L = R \| R`; `L → * R \| id`; `R → L` | no | **no** | yes | yes | SLR has a spurious shift/reduce conflict in the state containing `S → L · = R` and `R → L ·`, for `=`. |
| `S → a A d \| b B d \| a B e \| b A e`; `A → c`; `B → c` | no | no | **no** | yes | Merging LALR states causes a reduce/reduce conflict: `r(A→c)/r(B→c)` for `d` and `e`. |
| `E → E + E \| id` | no | no | no | **no** | The grammar is ambiguous; LR(1) retains a shift/reduce conflict for `+` (unless the generator resolves it with a precedence declaration). |

The second row is the classic example of an LALR(1) grammar that is not
SLR(1). The third is the classic example of an LR(1) grammar that is not
LALR(1).

## Do LR(k) Parsers Exist?

Yes. `LR(k)` means that the input is read from left to right, the parser
reconstructs a rightmost derivation in reverse, and it may use at most `k`
upcoming lookahead terminals to make a decision. Thus LR(0), LR(1), LR(2), …
are all formally meaningful. In practice, `k = 1` is used almost exclusively:
larger `k` values increase table sizes, while classical LR theory states that
every LR(k) language for `k ≥ 1` has an equivalent LR(1) grammar after an
appropriate transformation.

The names `SLR(1)` and `LALR(1)` refer to specific practical methods of
building tables with one lookahead; they are not the same as full canonical
LR(1).

## Online Sources

- WPI, *Bottom-Up Parsing*: presents the same expression grammar, its 12 LR(0)
  states, the fact that it is not LR(0), and the rule for entering SLR
  reductions:
  <https://web.cs.wpi.edu/~kal/courses/compilers/module3/mybuparsing.html>
- East Carolina University, *LALR(1) parsers*: the classic “LALR(1), but not
  SLR(1)” example and an explanation of the apparent SLR conflict:
  <https://cs.ecu.edu/abrahamsonk/5220/spr16/Notes/Bottom-up/lalr.html>
- East Carolina University, *LR(1) parsers*: describes merging LR(1) states
  into LALR states and how merging can introduce a conflict:
  <https://cs.ecu.edu/abrahamsonk/5220/spr16/Notes/Bottom-up/lr1.html>
- University of Pennsylvania, *Introduction to LR-Parsing*: defines the role
  of `k` in LR(k) and its relationship to SLR(k) and LALR(k):
  <https://www.seas.upenn.edu/~cis5110/notes/cis511-sl9.pdf>

### Additional Sources: `S → L = R | R`

The following classic grammar is LALR(1) and LR(1), but not SLR(1):

```text
S → L = R | R
L → * R | id
R → L
```

- Yale, *Compiler and Interpreters — Parser Generation*: LR(0) sets and the
  SLR shift/reduce conflict for `=` in the state containing `S → L · = R` and
  `R → L ·`:
  <https://flint.cs.yale.edu/cs421/lectureNotes/Spring14/c06.pdf>
- University of San Francisco, *LR Parsing*: complete states and the canonical
  LR(1) table for exactly this grammar:
  <https://www.cs.usfca.edu/~galles/cs414/lecture/lecture5.java.printable.pdf>
- Purdue, *The role of the parser*: construction of LR(1) item sets for this
  grammar and an explanation of why lookahead removes the conflict:
  <https://www.cs.purdue.edu/homes/hosking/502/notes/03-parse.pdf>
- RWTH Aachen, *Compiler Construction, lecture 10*: LR(1) items and table for a
  variant using the terminal `a` instead of `id`:
  <https://moves.rwth-aachen.de/wp-content/uploads/WS1819/cc/slides/l10.pdf>
- University of Alaska Fairbanks, *LR Parsers*: comparison of LR(0), SLR,
  LALR, and LR(1), including this grammar:
  <https://www.cs.uaf.edu/~cs631/notes/parse/node5.html>
