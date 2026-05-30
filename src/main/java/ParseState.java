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
    boolean escape = false;

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
        if (state.escape) {
            state.sb.append(character);
            state.escape = false;
            return;
        }
        switch (character) {
            case '\'' -> state.mode = Mode.SINGLE;
            case '\"' -> state.mode = Mode.DOUBLE;
            case ' ' -> {
                if (!state.sb.isEmpty()) flushToken(state);
            }
            case '\\' -> state.escape = true;
            default -> state.sb.append(character);
        }
    }

    public static void handleSingle(ParseState state, char character) {
        if (character == '\'') state.mode = Mode.NONE;
        else state.sb.append(character);
    }

    public static void handleDouble(ParseState state, char character) {
        if (state.escape) {
            state.sb.append(character);
            state.escape = false;
            return;
        }
        if (character == '\"') state.mode = Mode.NONE;
        if (character == '\\') state.escape = true;
        else state.sb.append(character);
    }
}
