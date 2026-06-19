package completion;

import java.util.ArrayList;

public record CompletionResult(
    ArrayList<String> candidates,
    String replacementText,
    boolean shouldAppendSpace,
    Status status
) {
    public enum Status {
        NO_MATCH,
        SINGLE_MATCH,
        PARTIAL_COMMON_PREFIX,
        AMBIGUOUS,
    }
}
