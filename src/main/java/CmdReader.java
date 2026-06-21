import completion.Completer;
import completion.CompletionRequest;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class CmdReader {

    private StringBuilder buffer = new StringBuilder();
    private int cursorIndex = 0;
    private Completer completer;
    private boolean pressedTabOnes = false;

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
        var result = completer.complete(
            new CompletionRequest(buffer.toString(), cursorIndex, currentDir)
        );
        // TODO completer returns everything correct(have to test) handle tab should learn how to use Completion result and how to move and print and such also finish FileSystemProvider

        switch (result.status()) {
            case SINGLE_MATCH, PARTIAL_COMMON_PREFIX -> handleCompletion(
                result.replacementText(),
                result.shouldAppendSpace()
            );
            case NO_MATCH -> {
                System.out.print('\u0007');
                System.out.flush();
            }
            case AMBIGUOUS -> handleAmbiguous(result.candidates());
        }
    }

    private void handleAmbiguous(ArrayList<String> list) {
        if (!pressedTabOnes) {
            System.out.print('\u0007');
            System.out.flush();
            pressedTabOnes = true;
        } else {
            pressedTabOnes = false;
            System.out.print("\r\n\r");
            list.stream()
                .sorted()
                .forEach(candidate -> System.out.print(candidate + "  "));
            System.out.print("\r\n\r");
            System.out.print("$ ");
            System.out.print(buffer);
            System.out.flush();
        }
    }

    private void handleCompletion(String add, boolean appendSpace) {
        String remainder = buffer.substring(cursorIndex);
        buffer.insert(cursorIndex, add);
        cursorIndex += add.length();

        if (appendSpace) {
            buffer.insert(cursorIndex, " ");
            cursorIndex++;
            System.out.print("\033[K" + add + " " + remainder);
        } else {
            System.out.print("\033[K" + add + remainder);
        }
        System.out.print("\b".repeat(remainder.length()));
        System.out.flush();
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
