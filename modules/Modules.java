package modules;

import common.Environ;
import util.Output;
import modules.extmod.ModuleLoader;
import modules.jdkutil.JavaCompileSystem;
import modules.systemio.OutputDirectory;
import modules.systemio.UpdateBuilder;
import modules.systemio.Cleaner;

public class Modules {
    public static Environ env;

    public static int tryRun(String name, String argString) {
        switch (name) {
            case "loadmod": {
                Output.error("module_ldr", "Unimplemented command: loadmod");
                ModuleLoader.loadModule(argString);
                return -1;
            }

            // Copy the current working directory to <cwd>_build and operate there instead.
            // TODO: DEPRECATED!
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
            case "javac": {
                return JavaCompileSystem.build(env, 17, argString);
            }
            // Compile the given input files for use in a library, so force compile of all.
            case "javac_lib": {
                return JavaCompileSystem.buildLib(env, argString);
            }
            // Compile the given input files and the given main file for Java SE 8.
            case "javac_v8": {
                Output.warn("module_ldr", "Deprecated module: jcompv8.");
                return JavaCompileSystem.build(env, 8, argString);
            }
            // Build the output binary with the required classpath baked in.
            case "mkjar": {
                // TODO: buildJar
                return JavaCompileSystem.link(env, argString);
            }
            // Remove everything but the listed file extension.
            case "keep": {
                // TODO: Add argument to keep.
                Cleaner.keepReq(env);
                return 0;
            }
            // Copy the loader binary into the application folder.
            case "use_launcher": {
                Output.warn("module_ldr", "Moving javac_jdk8 and module_ldr into a custom module!");
                return JavaCompileSystem.injectLoader(env, argString);
            } 
            // Copy the generated application into the jbuilder folder.
            case "cpsledge": {
                Output.warn("module_ldr", "Development only!");
                // TODO: Back up the old sledge binary.
                UpdateBuilder.copyTo(env, argString);
                return 0;
            }
            // Log a debug message on screen.
            case "log": {
                Output.log("userlog", argString);
                return 0;
            }
            case "warn": {
                Output.warn("warn", argString);
                return 0;
            }
            default: {
                Output.error("module_ldr", "Module " + name + ", args \"" + argString + "\" not recognized.");
                Output.error("module_ldr", "Compilation Terminated.");
                return -1;
            }
        }
    }

    // TODO: External module linkage.
    public static int execCmd(String cmd, String argString) {
        return 1;
    }

    public static void setOperatingEnvironment(Environ e) {
        env = e;
    }
}
