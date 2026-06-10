import commands.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.*;

public class Shell {

    private String currentDir;
    private final Set<String> builtinCommands;
    private final Map<String, Command> COMMANDS = new HashMap<>();

    public Shell() {
        currentDir = System.getProperty("user.dir");
        builtinCommands = Set.of("echo", "type", "exit", "pwd", "cd");
        COMMANDS.put("echo", new EchoCommand());
        COMMANDS.put("type", new TypeCommand(builtinCommands));
        COMMANDS.put("exit", new ExitCommand());
        COMMANDS.put("pwd", new PwdCommand());
        COMMANDS.put("cd", new CdCommand());
    }

    public void runs() throws Exception {
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.print("$ ");
                var cmdReader = new CmdReader(builtinCommands);
                var input = cmdReader.readLine();
                var words = ParseState.parseInput(input);
                if (words.isEmpty()) continue;

                try (var context = buildContext(words)) {
                    var commandStr = words.removeFirst();
                    Command command = COMMANDS.get(commandStr);
                    if (command == null) {
                        command = new ExternalCommand(commandStr);
                    }
                    command.execute(words, context);
                    this.currentDir = context.getCurrentDirectory();
                }
            }
        }
    }

    private CommandContext buildContext(ArrayList<String> words)
        throws FileNotFoundException {
        System.err.println("DEBUG WORDS BEFORE: " + words);
        PrintStream stdout = System.out;
        PrintStream stderr = System.err;
        var lists = List.of(">", "1>", "2>", ">>", "1>>", "2>>");
        String operator = words
            .stream()
            .filter(word -> lists.contains(word))
            .findFirst()
            .orElse(null);

        System.err.println("DEBUG OPERATOR: " + operator);

        if (operator != null) {
            int index = words.indexOf(operator);
            String fileString = words.remove(index + 1);
            File file = Optional.of(new File(fileString))
                .filter(File::isAbsolute)
                .orElseGet(() -> new File(this.currentDir, fileString));
            switch (operator) {
                case ">", "1>" -> {
                    stdout = new PrintStream(new FileOutputStream(file, false));
                }
                case ">>", "1>>" -> {
                    stdout = new PrintStream(new FileOutputStream(file, true));
                }
                case "2>" -> {
                    stderr = new PrintStream(new FileOutputStream(file, false));
                }
                case "2>>" -> {
                    stderr = new PrintStream(new FileOutputStream(file, true));
                }
            }
            words.remove(index);
            System.err.println("DEBUG WORDS AFTER: " + words);
        }
        CommandContext context = new CommandContext(
            stdout,
            stderr,
            System.in,
            this.currentDir
        );
        return context;
    }

    public String getCurrentDir() {
        return currentDir;
    }

    public void setCurrentDir(String currentDir) {
        this.currentDir = currentDir;
    }
}
