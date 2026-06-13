package completion.providers;

import completion.CompletionRequest;
import java.util.List;

public interface CompletionProvider {
    public List<String> candidates(CompletionRequest request);
}
