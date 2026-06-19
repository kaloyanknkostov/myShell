# Project Review

Last updated: 2025-06-19

Codecrafters shell (Java 21). ~21 source files, JUnit 5. The `Command` + `CommandContext` boundary is sound; completion is moving toward the architecture described in `context.md` but is not finished end-to-end.

## Summary

| Area | Grade | Notes |
|------|-------|-------|
| Architecture | B− | Clear layers emerging; parsing, redirection, and completion still tangled |
| Correctness | D+ | Multiple crash paths on malformed input |
| Error handling | F | Exceptions swallowed or printed-and-ignored |
| Test coverage | D | Completion helpers only; no tests on shell core |
| Maintainability | C | Duplication, stubs, debug leftovers |

`mvn test` passes, but tests barely exercise production behavior.

---

## Findings

### Critical / high

- **Backspace at cursor position 0 crashes the reader.** `CmdReader.handleBackspace()` (`src/main/java/CmdReader.java:102`) checks `buffer.length() > 0` but not `cursorIndex > 0`. It decrements the cursor first, so at position 0 `cursorIndex` becomes `-1` and `deleteCharAt` throws. `handleLeftArrow` already guards correctly. Repro: type `abc`, move cursor to start, press backspace.

- **Redirection without a filename crashes execution.** `Shell.buildContext()` (`src/main/java/Shell.java:55`) calls `words.remove(index + 1)` without verifying a target token exists. Repro: `echo hi >`.

- **Top-level exceptions are silently swallowed.** `Main.main()` (`src/main/java/Main.java:7`) has an empty `catch`, so any uncaught exception in the REPL loop looks like a clean exit. `CmdReader.readLine()` (`src/main/java/CmdReader.java:43`) prints a stack trace and returns `""`, which the shell treats as an empty command rather than a failure.

- **`type` with no arguments crashes.** `TypeCommand.execute()` (`src/main/java/commands/TypeCommand.java:19`) calls `args.getFirst()` without checking for empty args → `NoSuchElementException`.

- **External commands can deadlock on large stderr.** `ExternalCommand.execute()` (`src/main/java/commands/ExternalCommand.java:29`) drains stdout fully before stderr. If the child fills the stderr pipe while stdout is still open, the process blocks indefinitely. Fix with concurrent stream draining or `redirectErrorStream`.

### Medium

- **Completion logic exists but the UI is not wired.** `Completer` and `BuiltinCommandProvider` work, but `CmdReader.handleTab()` (`src/main/java/CmdReader.java:122`) ignores `CompletionResult` status, replacement text, and redraw logic — it only prints `FOUND <candidate>` as a debug stub. `ExecutableCommandProvider` and `FileSystemProvider` are empty stubs. `Shell.setupCmdReader()` registers only the builtin provider, not executables.

- **Stale `currentDir` in `CmdReader`.** `Shell.setupCmdReader()` (`src/main/java/Shell.java:89`) passes `currentDir` once at startup. After `cd`, tab completion (especially once `FileSystemProvider` exists) will use the wrong directory. `context.md` recommends passing `currentDir` per `readLine` call.

- **`Completer` filters providers via `instanceof`.** `Completer.complete()` (`src/main/java/completion/Completer.java:24`) branches on `BuiltinCommandProvider`, `ExecutableCommandProvider`, and `FileSystemProvider` type checks. Adding a new provider requires editing `Completer`. Prefer separate provider lists or a capability on the interface.

- **Duplicated PATH resolution.** `TypeCommand.getFile()` and `ExternalCommand.getFile()` are nearly identical. Extract shared lookup logic.

- **`System.getenv("PATH")` can be null.** Both `TypeCommand` and `ExternalCommand` call `.split(":")` on the result without a null guard → NPE in unusual environments.

- **`buildContext` mutates tokens while opening files.** Parsing, redirection extraction, and I/O setup are combined in one method that mutates `words` in place. Hard to test and extend (multiple redirects, input redirection, `2>&1`). Introduce a `ParsedCommand { name, args, redirects }` result type.

- **Only the first redirection operator is handled.** `Shell.buildContext()` uses `findFirst()` on redirect tokens. `echo hi > out 2> err` only processes the first operator.

- **Escape-sequence reading uses `InputStream.available()`.** `CmdReader.handleEscapeSequence()` (`src/main/java/CmdReader.java:67`) relies on `available()` to detect arrow-key sequences. Unreliable on TTYs — arrow keys can be dropped under load or on some terminals. Prefer blocking reads or a byte-by-byte state machine after ESC.

### Low

- **Inconsistent packaging.** `Main`, `Shell`, `CmdReader`, and `ParseState` are in the default package; `commands.*` and `completion.*` are packaged.

- **Misleading field naming.** `Shell.COMMANDS` is an instance field, not a constant — should be `commands`.

- **`exit` ignores its status argument.** `ExitCommand.execute()` (`src/main/java/commands/ExitCommand.java:10`) always calls `System.exit(0)`.

- **`CompletionResult` missing replacement range.** `context.md` describes `replacementStart` / `replacementEnd`; the record only has `replacementText`. `CmdReader` will need ranges to splice the buffer without re-deriving token boundaries.

- **Duplicated token-boundary logic in `CompletionRequest`.** `currentToken()` and `isCommand()` share the same backward scan — extract once.

- **Debug artifact in tab handler.** `System.out.println("FOUND " + list.getFirst())` in `handleTab` should be removed before submit.

- **`CdCommand` does not guard null `HOME`.** `new File(System.getenv("HOME"))` when `HOME` is unset produces surprising behavior.

---

## Architecture

### What works

- `Command` interface with per-command classes — easy to extend.
- `CommandContext` as `AutoCloseable` — redirected streams close correctly.
- `ParseState` static handlers — testable structure (just needs tests).
- `CompletionRequest` / `CompletionResult` records — good direction.
- `BuiltinCommandProvider` — simple and correct.
- `Completer.findCommonPrefix()` — solid, with unit tests.

### Target boundaries (from `context.md`)

```
Shell → CmdReader → Completer → Providers
```

- **Shell** wires commands, creates the completer, passes fresh `currentDir` each line.
- **CmdReader** owns buffer/cursor, calls completer on Tab, applies `CompletionResult`, redraws the line. Must not print completion decisions itself beyond terminal redraw.
- **Completer** decides what completion means; returns `CompletionResult` only.
- **Providers** supply candidates (builtins, PATH executables, filesystem).

Avoid: `Completer → Shell`, `CmdReader → CdCommand`, or any reverse dependency.

### Recommended next structural steps

1. Extract `ParsedCommand { name, args, redirects }` from `Shell.buildContext` + `ParseState.parseInput`.
2. Pass `currentDir` into `CmdReader.readLine(String currentDir)` (or update the reader after each command).
3. Wire `CmdReader.handleTab()` to apply `CompletionResult` (buffer splice, space append, double-Tab candidate list).
4. Implement `ExecutableCommandProvider`; register it in `Shell.setupCmdReader()`.
5. Add `replacementStart` / `replacementEnd` to `CompletionResult` (or derive consistently in one place).

---

## Test coverage

| Component | Tests | Gap |
|-----------|-------|-----|
| `Completer.findCommonPrefix` | 6 | Good |
| `CompletionRequest` | 5 | Good |
| `Completer.complete()` | None | Status transitions untested |
| `BuiltinCommandProvider` | None | — |
| `ParseState` | None | Quoting is subtle; highest ROI |
| `Shell.buildContext` | None | Redirection edge cases |
| `CmdReader` | None | Needs extracted logic or PTY integration tests |
| Commands (`type`, `cd`, `ExternalCommand`) | None | PATH lookup, `cd` errors |

`CompleterTest` only exercises package-private `findCommonPrefix`, not the public `complete()` API.

---

## Prioritized fix order

1. Crash fixes — backspace guard, redirection bounds, `type` empty args, null `PATH`.
2. Error visibility — stop swallowing exceptions in `Main` and `CmdReader`.
3. External command deadlock — concurrent stream draining.
4. Finish completion loop — apply `CompletionResult` in `CmdReader`, implement `ExecutableCommandProvider`, pass fresh `currentDir`.
5. Extract parser — `ParseState` → `ParsedCommand` with redirects.
6. Tests — `ParseState` quoting table, `Completer.complete()` scenarios, redirection unit tests.

---

## Verification

- `mvn -q test` passes (11 tests under `src/test/java/completion/`).
- Manually reproducible: backspace-at-cursor-0 crash, malformed redirection crash, `type` with no args crash.
- Completion tab currently prints debug output instead of completing.
