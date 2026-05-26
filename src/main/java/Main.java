import java.io.File;
import java.util.Arrays;
import java.util.Objects;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        boolean exit = false;
        Scanner scanner = new Scanner(System.in);
        while (!exit) {
            System.out.print("$ ");
            String input = scanner.nextLine();

            String words[] = input.split(" ");
            String command = words[0];
            String rest[] = Arrays.copyOfRange(words, 1, words.length);
            String result = String.join(" ", rest);


            if (Objects.equals(command,"exit"))
                exit = true;
            else if (Objects.equals(command,"echo"))
                System.out.println(result);
            else if (Objects.equals(command,"type")) {

                System.out.println(type(result));

            }
            //maybe error
            else System.out.println(input+ ": command not found");
        }
        scanner.close();
    }


    public static String type(String command){
       String[] commands = {"exit","echo","type"};
       String path_commands = System.getenv("PATH");
       String[] path_command = path_commands.split(":");
       for(String text:commands){
            if(Objects.equals(text,command))
                return command+" is a shell builtin";
       }
       for(String path:path_command) {
           File file = new File(path, command);
           if (file.exists() && file.canExecute()) {
               return command + " is " + file.getAbsolutePath();
           }
       }
       return command+": not found";
    }
}
