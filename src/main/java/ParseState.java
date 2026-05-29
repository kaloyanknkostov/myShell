import java.util.ArrayList;

public class ParseState {

    private enum Mode {
        NONE,
        SINGLE,
        DOUBLE,
    }

    ArrayList<String> tokens = new ArrayList<>();
    StringBuilder sb = new StringBuilder();
    Mode mode = Mode.NONE;

    public static ArrayList<String> parseInput(String input) {
        ParseState state = new ParseState();
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            switch (state.mode) {
                case NONE -> handleNone(state, c);
                case SINGLE -> handleSingle(state, c);
                case DOUBLE -> handleDouble(state, c);
            }
        }
        flushToken(state);
        return state.tokens;
    }

    public static void flushToken(ParseState state) {
        if (!state.sb.isEmpty()) {
            state.tokens.add(state.sb.toString());
            state.sb.setLength(0);
        }
    }

    public static void handleNone(ParseState state, char character) {
        if (character == '\'') {
            state.mode = Mode.SINGLE;
        } else if (character == '\"') {
            state.mode = Mode.DOUBLE;
        } else if (character == ' ') {
            if (!state.sb.isEmpty()) flushToken(state);
        } else state.sb.append(character);
    }

    public static void handleSingle(ParseState state, char character) {
        if (character == '\'') state.mode = Mode.NONE;
        else state.sb.append(character);
    }

    public static void handleDouble(ParseState state, char character) {}
}
