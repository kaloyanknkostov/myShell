import commands.*;
import java.io.File;
import java.io.PrintStream;
import java.util.*;

public class Shell {

    private String currentDir;
    private final Set<String> builtinCommands;
    private final Map<String, Command> COMMANDS = new HashMap<>();

    public Shell() {
        currentDir = System.getProperty("user.dir");
        COMMANDS.put("echo", new EchoCommand());
        COMMANDS.put("exit", new ExitCommand());
        builtinCommands = Set.of("echo", "type", "exit", "pwd", "cd");
    }

    public void run() throws Exception {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("$ ");
            String input = scanner.nextLine();
            ArrayList<String> words = ParseState.parseInput(input);
            if (words.isEmpty()) {
                continue;
            }

            // value of the command
            PrintStream stdout = System.out;
            PrintStream fileOut = null;
            int index = words.indexOf(">");
            // redirected found
            if (index != -1) {
                System.out.println("redirection");
                fileOut = new PrintStream(words.remove(index + 1));
                stdout = new PrintStream(fileOut);
                words.remove(index);
            }

            Command command = COMMANDS.get(words.removeFirst());
            if (command != null) {
                CommandContext context = new CommandContext(
                    new PrintStream(stdout),
                    System.err,
                    System.in
                );
                try {
                    command.execute(words, context);
                } finally {
                    if (fileOut != null) {
                        fileOut.close();
                    }
                }
            } else {
                System.out.println("Not built in");
            }

            scanner.close();
        }
    }
    /* */
}
