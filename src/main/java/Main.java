import java.io.File;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.Scanner;

public class Main {
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
                case "pwd" -> System.out.println(System.getProperty("user.dir"));
                case "cd" -> cd(words[1]);
                default -> {
                    if (getFile(System.getenv("PATH").split(":"), command).isPresent()) {
                        Process process = Runtime.getRuntime().exec(input.split(" "));
                        process.getInputStream().transferTo(System.out);
                    } else {
                        System.out.println(input + ": command not found");
                    }
                }
            }
        }
        scanner.close();
    }
    public static void cd(String directory){
        File target = new File(directory);
        if (target.isDirectory())
            System.setProperty("user.dir", target.getPath());
        else System.out.println("cd: "+ directory+": No such file or directory");

    }

    public static String type(String command){
       String[] commands = {"exit","echo","type","pwd","cd"};
       for(String text:commands){
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
