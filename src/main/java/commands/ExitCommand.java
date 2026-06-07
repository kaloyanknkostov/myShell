package commands;

import java.util.ArrayList;

public class ExitCommand implements Command {

    @Override
    public void execute(ArrayList<String> args, CommandContext context) {
        // Exit the JVM immediately with code 0 (success)
        System.exit(0);
    }
}
