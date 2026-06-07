package commands;

import java.io.File;
import java.util.ArrayList;
import java.util.Optional;

public class ExternalCommand implements Command {

    private final String commandName;

    public ExternalCommand(String commandName) {
        this.commandName = commandName;
    }

    @Override
    public void execute(ArrayList<String> args, CommandContext context)
        throws Exception {
        Optional<File> executable = getFile(
            System.getenv("PATH").split(":"),
            commandName
        );
        if (executable.isPresent()) {
            ArrayList<String> fullCommand = new ArrayList<>();
            fullCommand.add(commandName);
            fullCommand.addAll(args);
            Process process = new ProcessBuilder(fullCommand)
                .directory(new File(context.getCurrentDirectory()))
                .start();
            process.getInputStream().transferTo(context.getStdout());
            process.getErrorStream().transferTo(context.getStderr());
            process.waitFor();
        } else {
            context.getStdout().println(commandName + ": command not found");
        }
    }

    private Optional<File> getFile(String[] path_command, String command) {
        for (String path : path_command) {
            File file = new File(path, command);
            if (file.exists() && file.canExecute()) {
                return Optional.of(file);
            }
        }
        return Optional.empty();
    }
}
