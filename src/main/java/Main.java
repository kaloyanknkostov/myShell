import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        boolean exit = false;
        while (!exit) {
            System.out.print("$ ");
            Scanner scanner = new Scanner(System.in);
            String command = scanner.nextLine();
            if (command.equals("exit"))
                exit = true;
            else if (command.startsWith("echo "))
                System.out.println(command.substring(5));
            else if (command.startsWith("type ")) {
                //echo, exit, and type
                if (command.substring(5).equals("echo")| command.substring(5).equals("type")|command.substring(5).equals("exit"))
                    System.out.println(command.substring(5)+" is a shell builtin");
                else System.out.println(command.substring(5)+": not found");

            }
            else System.out.println(command + ": command not found");
        }
    }
}
