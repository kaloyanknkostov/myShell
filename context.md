## Part 1: What is  CommandContext ?

   CommandContext  is a simple data container class. Its sole job
  is to hold the input and output channels (streams) that a
  command is allowed to use.

  By wrapping these streams in a class, we decouple the command
  (e.g.,  echo ) from the physical terminal or file system.
  ──────
  ### Part 2: The Fields Inside  CommandContext

  Inside the class, you only need:

  1.  stdout  (PrintStream): The output stream where the command
  sends normal output.
  2.  stderr  (PrintStream): The output stream where the command
  sends error messages.
  3.  stdin  (InputStream): The input stream where the command
  reads inputs (if needed).
  4.  streamsToClose  (List of Closeables): A list of any streams
  opened specifically for this command (like a file output stream)
  that must be closed when the command finishes.
  ──────
  ### Part 3: Where and How it is Created

  The Shell ( Main.java ) creates the context right before
  executing a command.

  The creation logic works like this:

  1. The shell reads the user input (e.g.,  echo hello > out.txt ).
  2. The shell parser splits this into:
      • Command:  echo
      • Arguments:  [hello]
      • Redirection target:  out.txt  (via  > )
  3. The shell instantiates a new  CommandContext :
      • If there is NO redirection: The shell passes  System.out
      and  System.err  directly into the context. No streams are
      added to the cleanup list.
      • If there IS redirection: The shell opens a
      FileOutputStream  to  out.txt , wraps it in a  PrintStream ,
      and puts it in the context as the  stdout . It also adds
      this file stream to the context's cleanup list.

  ──────
  ### Part 4: How it is Used (The Lifecycle)

  We use Java's  try-with-resources  to manage the context's
  lifecycle:

    // 1. Shell creates the context
    try (CommandContext context = buildContext(parsedInput)) {

        // 2. Shell runs the command
        command.execute(args, context);

    } // 3. The context is automatically closed here!

  When  context.close()  runs, it loops through the
  streamsToClose  list and safely closes them.
  ──────
  ### Part 5: Walkthrough - Standard Output ( echo hello )

  Here is exactly what happens when you run a standard command:

    [User Input: "echo hello"]
          │
          ▼
    [Shell Parser] ──► Command: "echo", Args: ["hello"], Redirect:
  None
          │
          ▼
    [Context Creation] ──► stdout = System.out (Console)
          │
          ▼
    [EchoCommand.execute(args, context)]
          │
          ▼
       Calls: context.getStdout().println("hello")
          │
          ▼
    [Console prints "hello"]
          │
          ▼
    [Context Closes] ──► (No custom streams to close, does
  nothing)
  ──────
  ### Part 6: Walkthrough - Redirection ( echo hello > out.txt )

  Here is exactly what happens when you redirect output:

    [User Input: "echo hello > out.txt"]
          │
          ▼
    [Shell Parser] ──► Command: "echo", Args: ["hello"], Redirect:
  "out.txt"
          │
          ▼
    [Context Creation]
      1. Opens FileOutputStream to "out.txt"
      2. Wraps it in PrintStream
      3. Sets stdout = this PrintStream
      4. Adds FileOutputStream to closeable list
          │
          ▼
    [EchoCommand.execute(args, context)]
          │
          ▼
       Calls: context.getStdout().println("hello")
          │
          ▼
    [File "out.txt" receives "hello"]
          │
          ▼
    [Context Closes] ──► Closes the FileOutputStream, saving the
  file to disk.

  Notice that the  EchoCommand  code is identical in both
  walkthroughs. It has no idea the stream changed; it just calls
  println 
  __________________________________________________________________________________
  
ublic static CommandContext buildContext(ParsedCommand parsed) {
        if (parsed.hasRedirection()) {
            // If > was found, create the file-redirected context
            File file = new File(parsed.getRedirectFile());
            return CommandContext.createWithRedirection(file);
        } else {
            // If no > was found, create the standard console context
            return CommandContext.createDefault();
        }
    }

  Then, in your main shell loop, you can use it in a single line with
  try-with-resources :

    try (CommandContext context = buildContext(parsedCommand)) {
        // Look up the command (e.g., EchoCommand) and run it
        Command command = registry.get(parsedCommand.getName());
        command.execute(parsedCommand.getArguments(), context);
    }
