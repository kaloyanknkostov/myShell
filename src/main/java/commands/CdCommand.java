package commands;

import java.io.File;
import java.util.ArrayList;

public class CdCommand implements Command {

    @Override
    public void execute(ArrayList<String> args, CommandContext context)
        throws Exception {
        if (args.isEmpty()) {
            return;
        }

        String directory = args.getFirst();
        File target;

        if (directory.equals("~")) {
            target = new File(System.getenv("HOME"));
        } else {
            target = new File(directory);
        }

        if (!target.isAbsolute()) {
            target = new File(context.getCurrentDirectory(), directory);
        }

        if (target.isDirectory()) {
            context.setCurrentDirectory(target.getCanonicalPath());
        } else {
            context
                .getStderr()
                .println("cd: " + directory + ": No such file or directory");
        }
    }
}
