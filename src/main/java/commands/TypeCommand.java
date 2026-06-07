package commands;

import java.io.File;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Set;

public class TypeCommand implements Command {

    private final Set<String> builtinCommands;

    public TypeCommand(Set<String> set) {
        this.builtinCommands = set;
    }

    @Override
    public void execute(ArrayList<String> args, CommandContext context)
        throws Exception {
        var command = args.getFirst();
        String out;
        if (builtinCommands.contains(command)) out =
            command + " is a shell builtin";
        else out = getFile(System.getenv("PATH").split(":"), command)
            .map(f -> command + " is " + f.getAbsolutePath())
            .orElse(command + ": not found");
        context.getStdout().println(out);
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
