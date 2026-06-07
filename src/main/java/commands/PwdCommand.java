package commands;

import java.util.ArrayList;

public class PwdCommand implements Command {

    public PwdCommand() {}

    @Override
    public void execute(ArrayList<String> args, CommandContext context)
        throws Exception {
        context.getStdout().println(context.getCurrentDirectory());
    }
}
