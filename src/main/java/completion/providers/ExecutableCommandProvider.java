package completion.providers;

import completion.CompletionRequest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Stream;

public class ExecutableCommandProvider implements CompletionProvider {

    public ExecutableCommandProvider() {}

    @Override
    public List<String> candidates(CompletionRequest request) {
        var output = new LinkedHashSet<String>();
        for (String path : Arrays.asList(System.getenv("PATH").split(":"))) {
            try (Stream<Path> stream = Files.list(Paths.get(path))) {
                stream
                    .filter(file -> !Files.isDirectory(file))
                    .filter(file -> Files.isExecutable(file))
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .filter(name -> name.startsWith(request.currentToken()))
                    .forEach(output::add);
            } catch (IOException e) {}
        }
        return new ArrayList<>(output);
    }
}
