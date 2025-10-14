package modules.systemio;

import com.rsc_games.sledge.lib.LogModule;

import common.Environ;
import common.Path;

public class Cleaner {
    public static void cleanExt(Environ env, String ext) {
        LogModule.log("purge", "Stripping files of type " + ext);
        Path cleanPath = new Path(env);
        cleanPath.stripFileExt(ext);
    }
    
    public static void keepReq(Environ env) {
        LogModule.log("keepReq", "Cleaning build directory...");
        Path cleanPath = new Path(env);
        cleanPath.stripAllButJar();
    }
}
