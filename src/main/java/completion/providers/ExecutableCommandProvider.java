package completion.providers;

import completion.CompletionRequest;
import java.util.ArrayList;
import java.util.List;

public class ExecutableCommandProvider implements CompletionProvider {

    public ExecutableCommandProvider() {}

    @Override
    public List<String> candidates(CompletionRequest request) {
        var output = new ArrayList<String>();
        return output;
    }
}
