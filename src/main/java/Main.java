import java.io.File;
import java.util.*;

public class Main {

    private static String currentDir = System.getProperty("user.dir");
    private static final Set<String> builtinCommands = Set.of(
        "echo",
        "type",
        "exit",
        "pwd",
        "cd"
    );

    public static void main(String[] args) throws Exception {
        boolean exit = false;
        Scanner scanner = new Scanner(System.in);
        while (!exit) {
            System.out.print("$ ");
            String input = scanner.nextLine();
            ArrayList<String> words = ParseState.parseInput(input);
            if (words.isEmpty()) {
                continue;
            }
            String command = words.removeFirst();
            switch (command) {
                case "exit" -> exit = true;
                case "echo" -> System.out.println(String.join(" ", words));
                case "type" -> System.out.println(type(words.getFirst()));
                case "pwd" -> System.out.println(currentDir);
                // TODO error if only cd
                case "cd" -> cd(words.getFirst());
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
        }
        scanner.close();
    }

    public static void cd(String directory) throws java.io.IOException {
        File target;
        if (directory.startsWith("~")) target = new File(System.getenv("HOME"));
        else target = new File(directory);
        if (!target.isAbsolute()) {
            target = new File(currentDir, directory);
        }
        if (target.isDirectory()) {
            currentDir = target.getCanonicalPath();
        } else {
            System.out.println(
                "cd: " + directory + ": No such file or directory"
            );
        }
    }

    public static String type(String command) {
        for (String text : builtinCommands) {
            if (Objects.equals(text, command)) return (
                command + " is a shell builtin"
            );
        }
        return getFile(System.getenv("PATH").split(":"), command)
            .map(f -> command + " is " + f.getAbsolutePath())
            .orElse(command + ": not found");
    }

    public static Optional<File> getFile(
        String[] path_command,
        String command
    ) {
        for (String path : path_command) {
            File file = new File(path, command);
            if (file.exists() && file.canExecute()) {
                return Optional.of(file);
            }
        }
        return Optional.empty();
    }
}
