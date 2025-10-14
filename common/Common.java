package common;

import com.rsc_games.sledge.lib.LogModule;

public class Common {
    public static void fatalError(String message, String module) {
        LogModule.critical("build.fail", "FATAL: An error occurred during application build.");
        LogModule.error(module, "Error details: " + message);
        LogModule.error("build.fail", "Compilation failed.");
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
