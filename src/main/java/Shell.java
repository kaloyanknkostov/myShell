import commands.*;
import completion.Completer;
import completion.providers.BuiltinCommandProvider;
import completion.providers.CompletionProvider;
import completion.providers.ExecutableCommandProvider;
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
        setupCommands();
    }

    public void runs() throws Exception {
        var cmdReader = setupCmdReader();
        while (true) {
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

    private CommandContext buildContext(ArrayList<String> words)
        throws FileNotFoundException {
        PrintStream stdout = System.out;
        PrintStream stderr = System.err;
        var lists = List.of(">", "1>", "2>", ">>", "1>>", "2>>");
        String operator = words
            .stream()
            .filter(word -> lists.contains(word))
            .findFirst()
            .orElse(null);

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
        }
        CommandContext context = new CommandContext(
            stdout,
            stderr,
            System.in,
            this.currentDir
        );
        return context;
    }

    private CmdReader setupCmdReader() {
        var list = new ArrayList<CompletionProvider>();
        list.add(new BuiltinCommandProvider(builtinCommands));
        list.add(new ExecutableCommandProvider());
        return new CmdReader(new Completer(list), currentDir);
    }

    private void setupCommands() {
        COMMANDS.put("echo", new EchoCommand());
        COMMANDS.put("type", new TypeCommand(builtinCommands));
        COMMANDS.put("exit", new ExitCommand());
        COMMANDS.put("pwd", new PwdCommand());
        COMMANDS.put("cd", new CdCommand());
    }

    public String getCurrentDir() {
        return currentDir;
    }

    public void setCurrentDir(String currentDir) {
        this.currentDir = currentDir;
    }
}
