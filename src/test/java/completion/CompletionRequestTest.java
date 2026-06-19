package completion;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class CompletionRequestTest {

    @Test
    void getsPrefixWhenCursorIsAtEndOfCommand() {
        // create request with buffer "ech" and cursor 3
        // assert currentPrefix is "ech"
        var request = new CompletionRequest("ech", 3, "");
        assertEquals("ech", request.currentToken());
    }

    @Test
    void getsPrefixOnlyBeforeCursorWhenCursorIsInsideToken() {
        // create request with buffer "ech" and cursor 2
        // assert currentPrefix is "ec"
        var request = new CompletionRequest("ech", 2, "");
        assertEquals("ec", request.currentToken());
    }

    @Test
    void getsArgumentPrefixAfterCommandName() {
        // create request with buffer "cd foo" and cursor 4
        // assert currentPrefix is "f"
        var request = new CompletionRequest("cd foo", 4, "");
        assertEquals("f", request.currentToken());
    }

    @Test
    void testIsCommandOnCommand() {
        // create request with buffer "cd foo" and cursor 4
        // assert currentPrefix is "f"
        var request = new CompletionRequest("cd foo", 1, "");
        assertEquals(true, request.isCommand());
    }

    @Test
    void testIsCommandOnPath() {
        // create request with buffer "cd foo" and cursor 4
        // assert currentPrefix is "f"
        var request = new CompletionRequest("cd foo", 5, "");
        assertEquals(false, request.isCommand());
    }
}
