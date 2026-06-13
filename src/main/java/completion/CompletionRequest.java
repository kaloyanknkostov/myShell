package completion;

public class CompletionRequest {

    private String buffer;
    private int cursorIndex;
    private String currentDir;

    public CompletionRequest(
        String buffer,
        int cursorIndex,
        String currentDir
    ) {
        this.buffer = buffer;
        this.cursorIndex = cursorIndex;
        this.currentDir = currentDir;
    }
}
