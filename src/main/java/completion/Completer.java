package completion;

import completion.providers.CompletionProvider;
import java.util.ArrayList;

public class Completer {

    ArrayList<CompletionProvider> providers;

    public Completer(ArrayList<CompletionProvider> providers) {
        this.providers = providers;
    }

    public CompletionResult complete(CompletionRequest request) {
        var candidates = new ArrayList<String>();
        for (CompletionProvider provider : providers) {
            candidates.addAll(provider.candidates(request));
        }

        return new CompletionResult(
            candidates,
            1,
            1,
            "S",
            true,
            CompletionResult.Status.NO_MATCH
        );
    }
}
