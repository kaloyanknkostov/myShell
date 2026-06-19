import completion.Completer;
import completion.CompletionRequest;
import completion.CompletionResult;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

public class CmdReader {

    private StringBuilder buffer = new StringBuilder();
    private int cursorIndex = 0;
    private Completer completer;
    private String currentDir;

    public CmdReader(Completer completer, String currentDir) {
        this.completer = completer;
        this.currentDir = currentDir;
    }

    public String readLine() {
        String originalConfig = null;
        buffer.setLength(0);
        cursorIndex = 0;

        try {
            originalConfig = runStty("-g");
            runStty("raw -echo");
            System.out.print("$ ");
            System.out.flush();
            var in = System.in;
            int key;
            while ((key = in.read()) != -1) {
                if (key == '\r' || key == '\n') {
                    System.out.print("\r\n");
                    System.out.flush();
                    break;
                }
                handleKey(key, in);
            }

            return buffer.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        } finally {
            if (originalConfig != null) {
                try {
                    runStty(originalConfig);
                } catch (Exception ignored) {}
            }
        }
    }

    private void handleKey(int key, InputStream in) throws IOException {
        switch (key) {
            case 9 -> handleTab();
            case 127, 8 -> handleBackspace();
            case 27 -> handleEscapeSequence(in);
            default -> handleChar((char) key);
        }
    }

    private void handleEscapeSequence(InputStream in) throws IOException {
        int next1;
        int next2;
        if (in.available() > 0) {
            next1 = in.read(); // Read the '[' (91)
            if (in.available() > 0) {
                next2 = in.read(); // Read the direction (e.g. 68 for Left)
            } else return;
        } else return;

        if (next1 == '[') {
            switch (next2) {
                case 'D' -> handleLeftArrow();
                case 'C' -> handleRightArrow();
                case 'A' -> handleUpArrow();
                case 'B' -> handleDownArrow();
            }
        }
    }

    private void handleDownArrow() {}

    private void handleUpArrow() {}

    private void handleRightArrow() {
        if (cursorIndex < buffer.length()) {
            cursorIndex++;
            System.out.print("\033[C");
        }
    }

    private void handleLeftArrow() {
        if (cursorIndex > 0) {
            cursorIndex--;
            System.out.print("\033[D");
        }
    }

    private void handleBackspace() {
        if (buffer.length() > 0) {
            cursorIndex--;
            buffer.deleteCharAt(cursorIndex);
            String remainder = buffer.substring(cursorIndex);
            System.out.print("\b\033[K" + remainder);
            System.out.print("\b".repeat(remainder.length()));
            System.out.flush();
        }
    }

    private void handleChar(char ch) {
        String remainder = buffer.substring(cursorIndex);
        buffer.insert(cursorIndex, ch);
        cursorIndex++;
        System.out.print("\033[K" + ch + remainder);
        System.out.print("\b".repeat(remainder.length()));
        System.out.flush();
    }

    private void handleTab() {
        var list = completer
            .complete(
                new CompletionRequest(
                    buffer.toString(),
                    cursorIndex,
                    currentDir
                )
            )
            .candidates();
        // TODO completer returns everything correct(have to test) handle tab should learn how to use Completion result and how to move and print and such also finish FileSystemProvider
        // and execution provider
        if (!list.isEmpty()) System.out.println("FOUND " + list.getFirst());
    }

    private String runStty(String args)
        throws IOException, InterruptedException {
        Process cmd = new ProcessBuilder("/bin/sh", "-c", "stty " + args)
            .redirectInput(ProcessBuilder.Redirect.INHERIT)
            .start();
        cmd.waitFor();
        return new String(cmd.getInputStream().readAllBytes()).trim();
    }
}
