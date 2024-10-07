package modules.systemio;

import common.Environ;
import common.Path;
import util.Output;

public class Cleaner {
    public static void cleanExt(Environ env, String ext) {
        Output.log("purge", "Stripping files of type " + ext);
        Path cleanPath = new Path(env);
        cleanPath.stripFileExt(ext);
    }
    
    public static void keepReq(Environ env) {
        Output.log("keepReq", "Cleaning build directory...");
        Path cleanPath = new Path(env);
        cleanPath.stripAllButJar();
    }
}
