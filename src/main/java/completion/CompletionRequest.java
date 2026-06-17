package completion;

public class CompletionRequest {

    private String buffer;
    private int cursorIndex;
    private String currentDir;
    private String currentToken;

    public CompletionRequest(
        String buffer,
        int cursorIndex,
        String currentDir
    ) {
        this.buffer = buffer;
        this.cursorIndex = cursorIndex;
        this.currentDir = currentDir;
        this.currentToken = currentToken();
    }

    private String currentToken() {
        int start = cursorIndex - 1;
        for (int i = start; i >= 0; i--) {
            if (buffer.charAt(i) == ' ') {
                break;
            }
            start = i;
        }
        return buffer.substring(start, cursorIndex);
    }

    public String getBuffer() {
        return buffer;
    }

    public int getCursorIndex() {
        return cursorIndex;
    }

    public String getCurrentDir() {
        return currentDir;
    }

    public String getCurrentToken() {
        return currentToken;
    }
}
