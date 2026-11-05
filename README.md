# Zserio support for IntelliJ-based IDEs

An IntelliJ Platform plugin for [Zserio](https://zserio.org) schema files (`*.zs`).

## Supported Zserio versions

**Zserio 2.0 to 2.20** (the newest release checked is 2.20.0). Nothing has to be configured for this.

The language only grew between these releases: the compiler's grammar is identical from 2.12.0 to 2.20.0 and every
earlier release is a subset of it. The parser therefore accepts the syntax of the newest version, which also covers
schemas written for any older one. Zserio 1.x (with `rpc` and `stream`) is not supported.

Language elements which are newer than 2.0:

| Since | Element |
| --- | --- |
| 2.1 | `/*! ... !*/` markdown comments |
| 2.4 | `packed`, `rule_group` / `rule` |
| 2.5 | `zserio_compatibility_version("x.y.z");` |
| 2.8 | `isset(...)` |
| 2.9 | `bytes` |
| 2.12 | `extend`, `@deprecated`, `@removed` |

When a file declares `zserio_compatibility_version("x.y.z");`, elements newer than `x.y.z` are marked with a warning,
and a declared version outside the supported range is flagged. Files without the directive are not checked, since the
version they target is unknown. Keyword completion shows the release which introduced a keyword. The compiler
diagnostics (below) always apply the rules of the compiler you configured.

## Features

| Area | What works |
| --- | --- |
| Syntax | Lexer and parser (Grammar-Kit) for the complete language: packages, imports, constants, rule groups, subtypes, structures, choices, unions, enumerations, bitmasks, SQL tables and databases, services, pubsub, templates and `instantiate`, all expressions. Syntax errors are reported while typing. |
| Highlighting | Keywords, built-in types, literals, comments, and *semantic* colors for types, fields, parameters, functions, constants and enumeration items (configurable in Settings \| Editor \| Color Scheme \| Zserio). |
| Navigation | Go to declaration, find usages and rename for types, fields, parameters, functions, constants and enumeration / bitmask items, across files. Package and import rules are honored (same package, `import pkg.Type;`, `import pkg.*;`, qualified names). Member access is resolved through the types of fields, parameters, function results, subtypes and arrays (`a.b[0].c`, `Enum.ITEM`, `pkg.Enum.ITEM`, `case ITEM:` using the choice selector's type). Go to Class (Cmd+O) finds top-level types and Go to Symbol (Cmd+Alt+O) finds types, constants, fields, functions and enumeration / bitmask items, both by qualified name and backed by file-based indexes. |
| Completion | Keywords, types visible at the caret, members of expressions and after a dot. |
| Editing | Structure view, folding (bodies, comments, imports), brace matching, line / block commenting. |
| Compiler diagnostics | Optional. When `zserio.jar` is configured in Settings \| Tools \| Zserio, the compiler checks the open file and its errors and warnings are shown in the editor. Unsaved changes are checked too (without touching your project). |

## Build and run

Requirements: JDK 21. Everything else (Gradle 9.8, the IDE to build against) is downloaded by the Gradle wrapper.

```sh
./gradlew runIde            # start a sandbox IDE with the plugin
./gradlew test              # run the tests
./gradlew buildPlugin       # build/distributions/zserio-intellij-<version>.zip
```

Install the zip with *Settings | Plugins | ⚙ | Install Plugin from Disk…*.

The plugin is built against IntelliJ IDEA 2025.3 and is compatible with 2025.3 and newer
(`pluginSinceBuild` in `gradle.properties`). It only depends on the platform, so it works in all IntelliJ-based IDEs.

### Optional test inputs

```sh
# parse every *.zs below the given directories (path separated); there must not be any syntax error
./gradlew test --tests '*ZserioCorpusParsingTest*' -Pzserio.corpus=/path/to/zserio/benchmarks

# also run the tests against a real compiler
./gradlew test --tests '*ZserioCompilerTest*' -Pzserio.jar=/path/to/zserio.jar
```

Against the schemas in the Zserio repository (`test`, `benchmarks`, `compiler`) the parser accepts all 398 valid files
and rejects the 24 files which are syntactically invalid (`zserio.jar` rejects them as well).

## Project layout

```
src/main/grammar/Zserio.flex     JFlex lexer
src/main/grammar/Zserio.bnf      Grammar-Kit grammar; generates the parser and the PSI classes at build time
src/main/kotlin/.../psi          PSI helpers (named elements, kinds, members)
src/main/kotlin/.../reference    name resolution (packages, imports, scopes, type inference for member access)
src/main/kotlin/.../ide          highlighting, completion, folding, structure view, find usages, go to class / symbol (name indexes), compiler annotator
src/main/kotlin/.../compiler     running the Zserio compiler for diagnostics
src/main/kotlin/.../settings     Settings | Tools | Zserio
```

The lexer and the grammar are derived from the ANTLR grammar of the compiler
(`compiler/core/antlr/Zserio{Lexer,Parser}.g4`). The parser is a little more lenient than the compiler's (for example,
members of a compound type may be written in any order) so that the editor keeps working while code is incomplete.
Semantic checks are left to the compiler, see *Compiler diagnostics* above.

## Not done yet

- A code formatter.
- Live templates and quick fixes (for example "add import").
- Stub indexes for reference resolution: references are resolved by scanning all schema files of the project (cached
  until the next edit). That is fine for typical projects, an index would make it scale to very large ones. (Go to
  Class and Go to Symbol already use file-based indexes.)
- Documentation popups for `/** ... */` and `/*! ... !*/` comments.
- A user-visible indication when the compiler cannot be started.

## License

The grammar is derived from Zserio, which is distributed under the BSD 3-Clause License (see `LICENSE`).
