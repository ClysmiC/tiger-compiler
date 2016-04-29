# tiger-compiler
CS 4240 compiler project

# Documentation

The Tiger specification was initially given to us, and slightly changed throughout the course of the semester, so there isn't exactly one document that has the full specification. The Phase I document has the most thorough spec, but some of the details were changed in the Phase II, Typing Rules, and Phase III documents.

[Phase I (Language Specification, Scanning and Parsing)](http://alsmith.net/pdf/tiger-phase-i.pdf)

[Phase II (Symbol Table, Semantic Checking, IR Generation)](http://alsmith.net/pdf/tiger-phase-ii.pdf)

[Typing Rules Clarification (Issued During Phase II)](http://alsmith.net/pdf/tiger-typing-rules.pdf)

[Phase III (Assembly Generation, Register Allocation)](http://alsmith.net/pdf/tiger-phase-iii.pdf)

For documentation about the compiler implementation, view the following documents in the repository:
```
project_report_phase_i.pdf
project_report_phase_ii.pdf
project_report_phase_iii.pdf
```

## Hand-derived Tables

[DFA Table](https://docs.google.com/spreadsheets/d/1_1D4ODh1WIlEXVkq5lhb7fC2aj66BK4ejKXNWZQB8xo/edit?usp=sharing)

[Grammar Rules](https://docs.google.com/spreadsheets/d/1KAeba85UlM8cFErXpPCkI5ty_hcIva2GTGMIoFKv2Rc/edit?usp=sharing)

[First and Follow Sets](https://docs.google.com/spreadsheets/d/15z6mtT_9bQShhIu-pm_m6SGcUGD_5zS5EWDb4Ei8_34/edit?usp=sharing)

[Parser Table](https://docs.google.com/spreadsheets/d/1czimxFHXP-aTycIUk5kBjHONp5uYEVy04Gn_wQKKT6w/edit?usp=sharing)

## Compiling and Running

Compile on Linux:

```
compile-script.sh
```

Compile on Windows

```
compile-script.bat
```

Run (from project root directory). Use -h for full flag details:

```
java -cp src com.tiger.compiler.TigerCompiler <input-file> [flags]
```