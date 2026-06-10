import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

public class CmdReader {

    private StringBuilder buffer = new StringBuilder();
    private final Set<String> builtinCommands;
    private int cursorIndex = 0;

    public CmdReader(Set<String> builtinCommands) {
        this.builtinCommands = builtinCommands;
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
        if (buffer.toString().equals("ech")) {
            buffer.append("o");
            cursorIndex++;
            System.out.print("o");
            buffer.append(" ");
            cursorIndex++;
            System.out.print(" ");
            System.out.flush();
        }
        if (buffer.toString().equals("exi")) {
            buffer.append("t");
            cursorIndex++;
            System.out.print("t");
            buffer.append(" ");
            cursorIndex++;
            System.out.print(" ");
            System.out.flush();
        }
    }

    private String runStty(String args)
        throws IOException, InterruptedException {
        String[] sttyArgs = args.split(" ");
        String[] command = new String[sttyArgs.length + 1];
        command[0] = "stty";
        System.arraycopy(sttyArgs, 0, command, 1, sttyArgs.length);
        Process cmd = new ProcessBuilder(command)
            .redirectInput(new File("/dev/tty"))
            .start();
        int exitCode = cmd.waitFor();
        if (exitCode != 0) {
            String error = new String(
                cmd.getErrorStream().readAllBytes()
            ).trim();
            throw new IOException("stty failed: " + error);
        }
        return new String(cmd.getInputStream().readAllBytes()).trim();
    }
}
