package commands;

import java.io.InputStream;
import java.io.PrintStream;

public class CommandContext implements AutoCloseable {

    private final PrintStream stdout;
    private final PrintStream stderr;
    private final InputStream stdin;
    private String currentDirectory;

    public CommandContext(
        PrintStream stdout,
        PrintStream stderr,
        InputStream stdin,
        String currentDirectory
    ) {
        this.stdout = stdout;
        this.stderr = stderr;
        this.stdin = stdin;
        this.currentDirectory = currentDirectory;
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

    public String getCurrentDirectory() {
        return currentDirectory;
    }

    public void setCurrentDirectory(String currentDirectory) {
        this.currentDirectory = currentDirectory;
    }

    @Override
    public void close() throws Exception {
        if (stdout != System.out) {
            stdout.close();
        }
        if (stderr != System.err) {
            stderr.close();
        }
        if (stdin != System.in) {
            try {
                stdin.close();
            } catch (java.io.IOException e) {
                // Log or ignore the exception
            }
        }
    }
}
