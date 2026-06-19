package completion;

import completion.providers.BuiltinCommandProvider;
import completion.providers.CompletionProvider;
import completion.providers.ExecutableCommandProvider;
import completion.providers.FileSystemProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class Completer {

    private List<CompletionProvider> providers;

    public Completer(ArrayList<CompletionProvider> providers) {
        this.providers = providers;
    }

    public CompletionResult complete(CompletionRequest request) {
        ArrayList<String> candidates;
        String replacementText;
        boolean shouldAppendSpace;
        CompletionResult.Status status;
        if (request.isCommand()) {
            candidates = getCandidates(
                request,
                item ->
                    item instanceof BuiltinCommandProvider ||
                    item instanceof ExecutableCommandProvider
            );
            shouldAppendSpace = true;
        } else {
            candidates = getCandidates(
                request,
                item -> item instanceof FileSystemProvider
            );
            shouldAppendSpace = false;
        }
        if (candidates.isEmpty()) {
            replacementText = "";
            shouldAppendSpace = false;
            status = CompletionResult.Status.NO_MATCH;
        } else if (candidates.size() == 1) {
            replacementText = candidates.getFirst();
            status = CompletionResult.Status.SINGLE_MATCH;
        } else {
            String commonPrefix = findCommonPrefix(candidates);
            if (commonPrefix.length() > request.currentToken().length()) {
                replacementText = commonPrefix;
                shouldAppendSpace = false;
                status = CompletionResult.Status.PARTIAL_COMMON_PREFIX;
            } else {
                replacementText = "";
                shouldAppendSpace = false;
                status = CompletionResult.Status.AMBIGUOUS;
            }
        }

        return new CompletionResult(
            candidates,
            replacementText,
            shouldAppendSpace,
            status
        );
    }

    String findCommonPrefix(List<String> list) {
        String first = list.getFirst();
        for (int i = 0; i < first.length(); i++) {
            for (String compareTo : list) {
                if (
                    i >= compareTo.length() ||
                    first.charAt(i) != compareTo.charAt(i)
                ) {
                    return first.substring(0, i);
                }
            }
        }
        return first;
    }

    private ArrayList<String> getCandidates(
        CompletionRequest request,
        Predicate<CompletionProvider> allowed
    ) {
        var candidates = new ArrayList<String>();
        providers
            .stream()
            .filter(allowed)
            .map(item -> item.candidates(request))
            .forEach(providerCandidates ->
                candidates.addAll(providerCandidates)
            );
        return candidates;
    }
}
