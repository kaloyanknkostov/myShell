  Shell
    creates command registry
    creates Completer
    creates CmdReader
    passes currentDir into readLine()

  CmdReader
    reads keys
    owns buffer + cursor
    calls Completer when Tab is pressed
    applies completion result to buffer
    handles Tab-Tab display

  Completer
    receives CompletionRequest
    asks providers for candidates
    computes single match / common prefix / ambiguous result

  CompletionProvider
    interface for different completion sources

  BuiltinCommandProvider
    completes echo, exit, pwd, cd, type
  

  ExecutableCommandProvider
    completes executable names from PATH

  FileSystemProvider
    later: completes files/directories for arguments

  CompletionRequest
    describes current editing state

  CompletionResult
    describes what CmdReader should do

  The connection should be one-way:

  Shell -> CmdReader -> Completer -> Providers

  Avoid this direction:

  Completer -> Shell
  CmdReader -> CdCommand
  CdCommand -> CmdReader

  That would make the pieces too tangled.

  A possible class layout:

  Shell
    fields:
      currentDir
      commandMap
      builtinCommands
      completer

    methods:
      runs()
      buildContext(...)
      createCompleter()

  CmdReader
    fields:
      buffer
      cursorIndex
      completer
      lastCompletionRequest maybe
      previousKeyWasTab maybe

    methods:
      readLine(currentDir)
      handleKey(...)
      handleTab(currentDir)
      applyCompletion(...)
      showCompletionChoices(...)
      redrawLine(...)

  Completer
    fields:
      providers

    methods:
      complete(CompletionRequest request)
      longestCommonPrefix(matches)

  CompletionProvider
    method:
      candidates(CompletionRequest request)

  Then providers:

  BuiltinCommandProvider
    fields:
      builtinCommands

    method:
      candidates(request)

  ExecutableCommandProvider
    fields:
      pathDirectories maybe

    method:
      candidates(request)

  Later:

  FilePathCompletionProvider
    method:
      candidates(request)

  Your request object could contain:

  CompletionRequest
    buffer
    cursorIndex
    currentDirectory
    currentWord
    wordStartIndex
    wordEndIndex
    commandName maybe
    positionType: COMMAND_NAME or ARGUMENT

  The positionType is useful because this:

  ec<Tab>

  should complete command names, but this:

  echo sr<Tab>

  probably should complete file paths later.

  The result object could contain:

  CompletionResult
    status:
      NO_MATCH
      SINGLE_MATCH
      AMBIGUOUS
      PARTIAL_COMMON_PREFIX

    replacementStart
    replacementEnd
    replacementText
    candidates
    shouldAppendSpace

  Important: don’t make the completer print anything. It should only return information. CmdReader prints, redraws, inserts, and shows options.

  Example flow:

  User types: py<Tab>

  CmdReader:
    builds CompletionRequest from buffer/cursor/currentDir

  Completer:
    asks BuiltinCommandProvider
    asks ExecutableCommandProvider
    gets candidates: python, python3, pyenv
    computes longest common prefix: py
    returns AMBIGUOUS with candidates

  CmdReader:
    first Tab: beep or do nothing
    second Tab with same request: display candidates

  For this:

  User types: pyth<Tab>

  Completer:
    candidates: python, python3
    common prefix: python
    returns PARTIAL_COMMON_PREFIX replacementText = python

  Then CmdReader updates the buffer, but does not append a space because there are still multiple candidates.

  For this:

  User types: ech<Tab>

  Completer:
    candidates: echo
    returns SINGLE_MATCH replacementText = echo, shouldAppendSpace = true

  Then CmdReader turns:

  ech

  into:

  echo

  The most important boundary is:

  Completer decides what completion means.
  CmdReader decides how completion appears in the terminal.
  Providers decide where candidates come from.

  Summary: use Shell as the wiring place, CmdReader as the terminal/input editor, Completer as the decision engine, and provider classes for builtins, executables, and later files/directories.

  Next step: try sketching just the CompletionRequest and CompletionResult fields you think you need, without implementing providers yet. Send me that sketch and I’ll help you check whether the responsibilities are clean.
