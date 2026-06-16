# Project Review

## Findings

- High: Backspace at cursor position `0` crashes the reader. `src/main/java/CmdReader.java:102` checks only `buffer.length() > 0`, then decrements `cursorIndex`; if the cursor is at the start, it becomes `-1`. Reproduced with `abc`, left-left-left, backspace.
- High: Top-level exceptions are swallowed, so real crashes look like clean exits. `src/main/java/Main.java:7` has an empty `catch`, and `src/main/java/CmdReader.java:43` prints stack traces then returns an empty command. This hides bugs like `type` with no args or malformed redirection.
- High: Redirection without a filename crashes shell execution. `src/main/java/Shell.java:55` does `words.remove(index + 1)` without checking that a target exists. Reproduced with `echo hi >`.
- Medium: `type` with no args crashes due to `args.getFirst()`. See `src/main/java/commands/TypeCommand.java:19`. This should probably print a usage/error or no-op.
- Medium: External commands can deadlock on large stderr output. `src/main/java/commands/ExternalCommand.java:29` drains stdout fully before stderr. If the child fills stderr while stdout is still open, the process may block. Use concurrent stream draining or `redirectOutput` / `redirectError`.
- Medium: Completion architecture is scaffolded but not wired. `src/main/java/CmdReader.java:122` creates a `CompletionRequest` but ignores it, then hardcodes only `ech` and `exi`. `Completer`, `BuiltinCommandProvider`, and `CompletionResult` are effectively empty.
- Low: `CmdReader` captures `currentDir` only once. `src/main/java/Shell.java:90` passes the initial directory into the reader, but after `cd`, completions would still use the old directory.
- Low: `exit` ignores its status argument. `src/main/java/commands/ExitCommand.java:10` always exits `0`.

## Architecture Improvements

- Keep the current `Command` + `CommandContext` boundary, but move parsing/redirection into a dedicated parser result like `ParsedCommand { name, args, redirects }`. Right now `Shell.buildContext` mutates the token list while also opening files.
- Make `CmdReader.readLine` accept the current directory per call, or update the reader after `cd`; do not store stale shell state inside the terminal editor.
- Finish the completion boundary already described in `context.md`: `Completer` should return a `CompletionResult`, while `CmdReader` only applies/redraws terminal state.
- Add focused tests around `ParseState`, redirection parsing, command dispatch, `cd`, `type`, and completion. There are currently no test files under `src`.

## Verification

- `mvn -q test` passes.
- Manually reproduced the cursor crash.
- Manually reproduced the malformed-redirection shell exit.
