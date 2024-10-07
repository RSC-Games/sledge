package util;

import java.util.*;

public class Input {
    private static Scanner s;

    public static void init() {
        s = new Scanner(System.in);
    }

    public static String input(String prompt) {
        System.out.print(prompt);
        return s.nextLine();
    }

    public static String input() {
        return s.nextLine();
    }

    public static int getInt() {
        int out = 0;
        while (true) {
            try {
                out = s.nextInt();
            }
            catch (InputMismatchException ie) {
                System.out.println("Please enter a number!");
                continue;
            }
            finally {
                s.nextLine();
            }
            return out;
        }
    }
}
