import commands.*;
import java.io.File;
import java.io.FileNotFoundException;
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
                var input = scanner.nextLine();
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
        PrintStream stdout = System.out;
        int index = words.indexOf(">");
        if (index != -1) {
            index = words.indexOf("1>");
        }
        if (index != -1) {
            String fileString = words.remove(index + 1);
            var file = new File(fileString);
            if (!file.isAbsolute()) {
                file = new File(this.currentDir, fileString);
            }
            stdout = new PrintStream(file);
            words.remove(index);
        }
        CommandContext context = new CommandContext(
            stdout,
            System.err,
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
