package completion.providers;

import completion.CompletionRequest;
import java.util.ArrayList;
import java.util.List;

public class FileSystemProvider implements CompletionProvider {

    public FileSystemProvider() {}

    @Override
    public List<String> candidates(CompletionRequest request) {
        var output = new ArrayList<String>();
        return output;
    }
}
