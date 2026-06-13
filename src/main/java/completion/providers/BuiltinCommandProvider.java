package completion.providers;

import completion.CompletionRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BuiltinCommandProvider implements CompletionProvider {

    private final Set<String> builtinCommands;

    public BuiltinCommandProvider(Set<String> builtinCommands) {
        this.builtinCommands = builtinCommands;
    }

    @Override
    public List<String> candidates(CompletionRequest request) {
        var output = new ArrayList<String>();

        return output;
    }
}
