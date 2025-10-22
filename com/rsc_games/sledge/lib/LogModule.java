package com.rsc_games.sledge.lib;

public class LogModule {
    public static void info(String unit, String message) {
        System.out.printf("\033[32m%s: %s\033[0m\n", unit, message);
    }

    public static void log(String unit, String message) {
        System.out.printf("%s: %s\n", unit, message);
    }

    public static void log(String unit, String message, boolean addnewline) {
        System.out.printf("%s: %s%s", unit, message, (addnewline ? "\n" : ""));
    }
    
    public static void warn(String unit, String message) {
        System.out.printf("\033[33m%s: %s\033[0m\n", unit, message);
    }

    public static void error(String unit, String message) {
        System.out.printf("\033[31m%s: %s\033[0m\n", unit, message);
    }

    public static void critical(String unit, String message) {
        System.out.printf("\033[91;48;2;195;195;1m%s: %s\033[0m\n", unit, message);
    }
}
