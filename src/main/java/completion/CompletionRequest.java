package completion;

public record CompletionRequest(
    String buffer,
    int cursorIndex,
    String currentDir
) {
    public String currentToken() {
        if (cursorIndex == 0) {
            return "";
        }
        int start = cursorIndex - 1;
        for (int i = start; i >= 0; i--) {
            if (buffer.charAt(i) == ' ') {
                break;
            }
            start = i;
        }
        return buffer.substring(start, cursorIndex);
    }

    public boolean isCommand() {
        if (cursorIndex == 0) {
            return true;
        }
        int start = cursorIndex - 1;
        for (int i = start; i >= 0; i--) {
            if (buffer.charAt(i) == ' ') {
                break;
            }
            start = i;
        }
        if (start == 0) return true;
        return false;
    }
}
