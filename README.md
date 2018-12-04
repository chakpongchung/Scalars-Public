# Scalars Decaf Compiler

Nov. 28, 2018 23:10 PM

## Scanner

- 100% pass on public tests
- 100% pass on private tests

## Parser

- 100% pass on public tests
- 100% pass on private tests

## IR/Semantics

- 100% pass on public tests
- 100% pass on private tests

## Code Generation

- 100% pass on public tests
- 100% pass on private tests

## Dataflow Analysis

- Implemented: local CSE. local CP, local DCE, global CSE, global CP, global DCE (see Optimization section)
- 100% pass on public tests
- 100% pass on private tests

## Optimization

### Current Progress

|              Name              | Implemented | Tested | Array Support  | RepeatOptimization Support |
| :----------------------------: | :---------: | :----: | :------------: | :------------------------: |
|           Local CSE            |     Yes     |  Yes   |      Yes       |            Yes             |
|            Local CP            |     Yes     |  Yes   | Not Applicable |            Yes             |
|           Local DCE            |     Yes     |  Yes   | Not Applicable |            Yes             |
|           Global CSE           |     Yes     |  Yes   |      Yes       |            Yes             |
|           Global CP            |     Yes     |  Yes   |      Yes       |            Yes             |
|           Global DCE           |     Yes     |  Yes   | Not Applicable |            Yes             |
|        Constant Folding        |     Yes     |  Yes   | Not Applicable |            Yes             |
|       Invariant Hoisting       |     Yes     |  Yes   | Not Applicable |            Yes             |
| Induction Variable Elimination |     WIP     |        |                |                            |
|      Register Allocation       |     WIP     |        |                |                            |

