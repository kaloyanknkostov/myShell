package completion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import completion.providers.BuiltinCommandProvider;
import completion.providers.CompletionProvider;
import completion.providers.FileSystemProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class CompleterTest {

    private static final Set<String> BUILTINS = Set.of(
        "echo",
        "type",
        "exit",
        "pwd",
        "cd"
    );

    ArrayList<CompletionProvider> providers = new ArrayList<>();
    Completer completer = new Completer(providers);

    @Test
    void testFirstSmallest() {
        assertEquals(
            "echo",
            completer.findCommonPrefix(
                new ArrayList<>(List.of("echo", "echonew", "echoj"))
            )
        );
    }

    @Test
    void testOutOfBounds() {
        assertEquals(
            "echo",
            completer.findCommonPrefix(
                new ArrayList<>(List.of("echof", "echo", "echojjksf"))
            )
        );
    }

    @Test
    void testFirstBiggest() {
        assertEquals(
            "echo",
            completer.findCommonPrefix(
                new ArrayList<>(List.of("echojklsjf", "echonew", "echoj"))
            )
        );
    }

    @Test
    void testGeneral() {
        assertEquals(
            "ec",
            completer.findCommonPrefix(
                new ArrayList<>(List.of("ecojklsjf", "echonew", "echoj"))
            )
        );
    }

    @Test
    void testVoid() {
        assertEquals(
            "",
            completer.findCommonPrefix(
                new ArrayList<>(List.of("fcojklsjf", "echonew", "echoj"))
            )
        );
    }

    @Test
    void testEmpty() {
        assertEquals(
            "",
            completer.findCommonPrefix(
                new ArrayList<>(List.of("", "echonew", "echoj"))
            )
        );
    }

    @Test
    void complete_noMatch_returnsNoMatchStatus() {
        var completer = commandCompleter();

        var result = completer.complete(new CompletionRequest("xyz", 3, ""));

        assertEquals(CompletionResult.Status.NO_MATCH, result.status());
        assertEquals("", result.replacementText());
        assertFalse(result.shouldAppendSpace());
        assertTrue(result.candidates().isEmpty());
    }

    @Test
    void complete_singleMatch_returnsSuffixOnly() {
        var completer = commandCompleter();

        var result = completer.complete(new CompletionRequest("ech", 3, ""));

        assertEquals(CompletionResult.Status.SINGLE_MATCH, result.status());
        assertEquals("o", result.replacementText());
        assertTrue(result.shouldAppendSpace());
        assertEquals(List.of("echo"), result.candidates());
    }

    @Test
    void complete_singleMatch_whenAlreadyComplete_returnsEmptySuffix() {
        var completer = commandCompleter();

        var result = completer.complete(new CompletionRequest("echo", 4, ""));

        assertEquals(CompletionResult.Status.SINGLE_MATCH, result.status());
        assertEquals("", result.replacementText());
        assertTrue(result.shouldAppendSpace());
        assertEquals(List.of("echo"), result.candidates());
    }

    @Test
    void complete_partialCommonPrefix_returnsSuffixBeyondTypedToken() {
        var completer = commandCompleterWith("python", "python3");

        var result = completer.complete(new CompletionRequest("pyth", 4, ""));

        assertEquals(
            CompletionResult.Status.PARTIAL_COMMON_PREFIX,
            result.status()
        );
        assertEquals("on", result.replacementText());
        assertFalse(result.shouldAppendSpace());
        assertEquals(Set.of("python", "python3"), Set.copyOf(result.candidates()));
    }

    @Test
    void complete_ambiguous_returnsEmptyReplacement() {
        var completer = commandCompleter();

        var result = completer.complete(new CompletionRequest("e", 1, ""));

        assertEquals(CompletionResult.Status.AMBIGUOUS, result.status());
        assertEquals("", result.replacementText());
        assertFalse(result.shouldAppendSpace());
        assertEquals(Set.of("echo", "exit"), Set.copyOf(result.candidates()));
    }

    @Test
    void complete_argumentSingleMatch_doesNotAppendSpace() {
        var completer = argumentCompleter("readme.txt", "notes.txt");

        var result = completer.complete(new CompletionRequest("cat re", 6, ""));

        assertEquals(CompletionResult.Status.SINGLE_MATCH, result.status());
        assertEquals("adme.txt", result.replacementText());
        assertFalse(result.shouldAppendSpace());
        assertEquals(List.of("readme.txt"), result.candidates());
    }

    @Test
    void complete_argumentPartialCommonPrefix_returnsSuffixBeyondTypedToken() {
        var completer = argumentCompleter("python", "python3");

        var result = completer.complete(
            new CompletionRequest("script pyth", 11, "")
        );

        assertEquals(
            CompletionResult.Status.PARTIAL_COMMON_PREFIX,
            result.status()
        );
        assertEquals("on", result.replacementText());
        assertFalse(result.shouldAppendSpace());
        assertEquals(Set.of("python", "python3"), Set.copyOf(result.candidates()));
    }

    private Completer commandCompleter() {
        var list = new ArrayList<CompletionProvider>();
        list.add(new BuiltinCommandProvider(BUILTINS));
        return new Completer(list);
    }

    private Completer commandCompleterWith(String... names) {
        var list = new ArrayList<CompletionProvider>();
        list.add(new StubCommandProvider(names));
        return new Completer(list);
    }

    private Completer argumentCompleter(String... names) {
        var list = new ArrayList<CompletionProvider>();
        list.add(new StubFileProvider(names));
        return new Completer(list);
    }

    private static class StubCommandProvider extends BuiltinCommandProvider {

        StubCommandProvider(String... names) {
            super(Set.of(names));
        }
    }

    private static class StubFileProvider extends FileSystemProvider {

        private final List<String> candidates;

        StubFileProvider(String... names) {
            this.candidates = List.of(names);
        }

        @Override
        public List<String> candidates(CompletionRequest request) {
            var output = new ArrayList<String>();
            for (String name : candidates) {
                if (name.startsWith(request.currentToken())) {
                    output.add(name);
                }
            }
            return output;
        }
    }
}
