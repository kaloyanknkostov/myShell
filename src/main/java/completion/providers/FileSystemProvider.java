package completion.providers;

import completion.CompletionRequest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Stream;

public class FileSystemProvider implements CompletionProvider {

    public FileSystemProvider() {}

    @Override
    public List<String> candidates(CompletionRequest request) {
        var output = new LinkedHashSet<String>();
        try (
            Stream<Path> stream = Files.list(Paths.get(request.currentDir()))
        ) {
            stream
                .filter(entity ->
                    entity
                        .getFileName()
                        .toString()
                        .startsWith(request.currentToken())
                )
                .map(item ->
                    Files.isDirectory(item)
                        ? item.getFileName().toString() + "/"
                        : item.getFileName().toString()
                )
                .forEach(output::add);
        } catch (IOException e) {}
        return new ArrayList<>(output);
    }
}
