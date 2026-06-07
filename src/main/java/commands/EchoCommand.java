package commands;

import java.util.ArrayList;

public class EchoCommand implements Command {

    @Override
    public void execute(ArrayList<String> args, CommandContext context) {
        context.getStdout().println(String.join(" ", args));
    }
}
