package util;

public class Output {
    public static void info(String file, String message) {
        System.out.println("\033[32m[INFO]: " + file + ": " + message + "\033[0m");
    }

    public static void log(String file, String message) {
        System.out.println("[JLOG]: " + file + ": " + message);
    }

    public static void log(String file, String message, boolean addnewline) {
        System.out.print("[JLOG]: " + file + ": " + message + (addnewline ? "\n" : ""));
    }
    
    public static void warn(String file, String message) {
        System.out.println("\033[33m[WARN]: " + file + ": " + message + "\033[0m");
    }

    public static void error(String file, String message) {
        System.out.println("\033[31m[ERROR]: " + file + ": " + message + "\033[0m");
    }

    public static void critical(String file, String message) {
        System.out.println("\033[31;43m[CRIT]: " + file + ": " + message + "\033[0m");
    }
}
