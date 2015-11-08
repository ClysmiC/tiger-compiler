# tiger-compiler
CS 4240 compiler project

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