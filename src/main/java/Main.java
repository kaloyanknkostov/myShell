import java.io.File;
import java.util.*;

public class Main {
    private static String currentDir = System.getProperty("user.dir");
    private static final Set<String> builtinCommands = Set.of("echo", "type", "exit", "pwd", "cd");

    public static void main(String[] args) throws Exception {
        boolean exit = false;
        Scanner scanner = new Scanner(System.in);
        while (!exit) {
            System.out.print("$ ");
            String input = scanner.nextLine();

            String[] words = input.split(" ");
            String command = words[0];
            String[] rest = Arrays.copyOfRange(words, 1, words.length);
            String result = String.join(" ", rest);


            switch (command) {
                case "exit" -> exit = true;
                case "echo" -> System.out.println(result);
                case "type" -> System.out.println(type(result));
                case "pwd" -> System.out.println(currentDir);
                // TODO error if only cd
                case "cd" -> cd(words[1]);
                default -> {
                    if (getFile(System.getenv("PATH").split(":"), command).isPresent()) {
                        Process process = new ProcessBuilder(input.split(" "))
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
        File target = new File(currentDir, directory);
        if (target.isDirectory()) {
            currentDir = target.getCanonicalPath();
        } else {
            System.out.println("cd: " + directory + ": No such file or directory");
        }
    }

    public static String type(String command){
       for(String text:builtinCommands){
            if(Objects.equals(text,command))
                return command+" is a shell builtin";
       }
       return getFile(System.getenv("PATH").split(":"), command)
            .map(f -> command + " is " + f.getAbsolutePath())
            .orElse(command + ": not found");
    }

    public static Optional<File> getFile(String[] path_command, String command){
        for(String path:path_command) {
            File file = new File(path, command);
            if (file.exists() && file.canExecute()) {
                return Optional.of(file);
            }
        }
        return Optional.empty();
    }
}
