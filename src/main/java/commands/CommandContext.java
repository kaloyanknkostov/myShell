package commands;

import java.io.InputStream;
import java.io.PrintStream;

public class CommandContext {

    private final PrintStream stdout;
    private final PrintStream stderr;
    private final InputStream stdin;

    public CommandContext(
        PrintStream stdout,
        PrintStream stderr,
        InputStream stdin
    ) {
        this.stdout = stdout;
        this.stderr = stderr;
        this.stdin = stdin;
    }

    public PrintStream getStdout() {
        return stdout;
    }

    public PrintStream getStderr() {
        return stderr;
    }

    public InputStream getStdin() {
        return stdin;
    }
}
