package completion;

import static org.junit.jupiter.api.Assertions.assertEquals;

import completion.providers.CompletionProvider;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class CompleterTest {

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
}
