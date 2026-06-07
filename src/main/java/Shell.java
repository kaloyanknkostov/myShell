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
        COMMANDS.put("exit", new ExitCommand());
        //COMMANDS.put("exit", new PwdCommand(currentDir));
        COMMANDS.put("type", new TypeCommand(builtinCommands));
    }

    public void runs() throws Exception {
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.print("$ ");
                var input = scanner.nextLine();
                var words = ParseState.parseInput(input);
                if (words.isEmpty()) {
                    continue;
                }
                var context = buildContext(words);
                Command command = COMMANDS.get(words.removeFirst());

                if (command != null) {
                    command.execute(words, context);
                } else {
                    System.out.println("oops");
                }
            }
        }
    }

    private CommandContext buildContext(ArrayList<String> words)
        throws FileNotFoundException {
        PrintStream stdout = System.out;
        int index = words.indexOf(">");
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
            System.in
        );
        return context;
    }

    /*
    default -> {
                        if (
                            getFile(
                                System.getenv("PATH").split(":"),
                                command
                            ).isPresent()
                        ) {
                            ArrayList<String> fullCommand = new ArrayList<>();
                            fullCommand.add(command);
                            fullCommand.addAll(words);
                            Process process = new ProcessBuilder(fullCommand)
                                .directory(new File(currentDir))
                                .start();
                            process.getInputStream().transferTo(System.out);
                        } else {
                            System.out.println(input + ": command not found");
                        }
                    }
                }
    */
}
