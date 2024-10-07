package modules;

import common.Environ;
import util.Output;
import modules.jdkutil.JavaCompileSystem;
import modules.systemio.OutputDirectory;
import modules.systemio.UpdateBuilder;
import modules.systemio.Cleaner;

public class Modules {
    public static Environ env;

    public static int tryRun(String name, String argString) {
        switch (name) {
            // Copy the current working directory to <cwd>_build and operate there instead.
            case "useoutdir": {
                OutputDirectory.genOutDir(env);
                return 0;
            }
            // Erase all entries of a given file type recursively from the current directory.
            case "purge": {
                Cleaner.cleanExt(env, argString);
                return 0;
            }
            // Compile the given input files and the given main file.
            case "jcomp": {
                return JavaCompileSystem.build(env, 17, argString);
            }
            // Compile the given input files for use in a library, so force compile of all.
            case "jcomplib": {
                return JavaCompileSystem.buildLib(env, argString);
            }
            // Compile the given input files and the given main file for Java SE 8.
            case "jcompv8": {
                Output.warn("module_ldr", "Deprecated module: jcompv8.");
                return JavaCompileSystem.build(env, 8, argString);
            }
            // Build the output binary with the required classpath baked in.
            case "link": {
                return JavaCompileSystem.link(env, argString);
            }
            // Clean everything but the jarfile.
            case "keepreq": {
                Cleaner.keepReq(env);
                return 0;
            }
            // Copy the loader binary into the application folder.
            case "linkldr": {
                return JavaCompileSystem.injectLoader(env, argString);
            } 
            // Copy the generated application into the jbuilder folder.
            case "cpjbuild": {
                UpdateBuilder.copyTo(env, argString);
                return 0;
            }
            // Log a debug message on screen.
            case "log": {
                Output.log("jbuildlog", argString);
                return 0;
            }
            default: {
                Output.error("module_ldr", "Unsupported module " + name + " with args " + argString);
                return -1;
            }
        }
    }

    public static int execCmd(String cmd, String argString) {
        return 1;
    }

    public static void setOperatingEnvironment(Environ e) {
        env = e;
    }
}
