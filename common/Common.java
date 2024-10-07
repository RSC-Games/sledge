package common;

import util.Output;

public class Common {
    public static void fatalError(String message, String module) {
        Output.critical("build.fail", "FATAL: An error occurred during application build.");
        Output.error(module, "Error details: " + message);
        Output.error("build.fail", "Compilation failed.");
        System.exit(1);
    }

    public static void sleep(int ms) {
        try { 
            Thread.sleep(ms); 
        }
        catch (InterruptedException ie) {
            System.out.println("Interrupted!");
        }
    }
}
