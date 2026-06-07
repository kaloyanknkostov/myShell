import commands.*;
import java.io.File;
import java.io.PrintStream;
import java.util.*;

public class Main {

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
        if (builtinCommands.contains(command)) return (
            command + " is a shell builtin"
        );

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
