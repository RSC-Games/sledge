package modules.systemio;

import java.io.IOException;
import java.io.File;
import common.Path;
import common.Environ;
import util.Output;

public class OutputDirectory {
    public static void genOutDir(Environ env) {
        String buildDir = env.getcwd() + "_build";
        env.setdir(buildDir);

        // Purge the old build directory.
        File oldBuild = new File(buildDir);

        if (oldBuild.exists()) {
            Output.log("outdir", "Purging old build directory: (" + buildDir + ")");
            Path bdir = new Path(env);
            bdir.recursiveDelete();
        }

        Output.log("outdir", "Making new build directory " + buildDir);
        Path destDir = new Path(env);
        
        try {
            destDir.copyFrom(env.projPath);
        }
        catch (IOException ie) {
            ie.printStackTrace();
            Output.critical("useoutdir", "Unit failed!");
            Output.error("jBuilder", "Compilation terminated.");
            System.exit(1);
        }

        Output.log("outdir", "Created build directory successfully.");
    }

/*
    // Will need to reimplement.
    public static String genBuildDir(ProcessedArgs buildEnviron) {
        // Copy the working directory into the build directory.
        String buildDir = buildEnviron.folderPath + "_build";

        File folder = new File(buildDir);
        if (folder.exists()) {
            // Erase the build directory if present. 
            Output.log("codegen", "Auto-purging build directory. (" + buildDir + ")");
            Path p = new Path(new Environ(buildDir));
            p.recursiveDelete();
        }

        Output.log("codegen", "Copying files to build directory (" + buildDir + ").");

        try {
            ProcessIO.init();

            final Process p = Runtime.getRuntime().exec("xcopy /E /I /Y \"" + buildEnviron.folderPath + 
                "\" \"" + buildDir + "\"");
            ProcessIO.startAsyncReadPipe(p);
            int r = p.waitFor();
            ProcessIO.stopReadPipe();

            if (r != 0) {
                Output.critical("codegen", "xcopy unit run failure.");
                Common.fatalError("External unit failed", "codegen");
            }

            // Place a copy of the build helper in the target directory.
            //Path path = new Path("jbuilder_javac_helper.jar");
            //path.copy(buildDir + "\\jbuilder_javac_helper.jar");
        }
        catch (FileNotFoundException ie) {
            ie.printStackTrace();
            Output.critical("codegen", "Required files not found.");
            Common.fatalError("External unit not found", "codegen");
        }
        catch (IOException ie) {
            ie.printStackTrace();
            Output.critical("codegen", "Copy operations failed.");
            Common.fatalError("External unit failed", "codegen");
        }
        catch (InterruptedException ie) {}

        Output.log("codegen.copy_dir", "Build directory copied successfully.");
        return buildDir;
    }

    public static void setupLauncher(ProcessedArgs args, String path) {
        ApploaderArgs ldargs = new ApploaderArgs(
            ApploaderArgs.STD_ARGS_FILE_VER, // Generated file defaults to latest args version.
            args.jarName + "_resources.jar", // Just generated jarfile name.
            true, // Write pipe is currently permanently enabled. May be changed in future versions.
            JRE_PATH, // Locked to one path as this is a toolchain tailored to the school PCs.
            0, // No argument list supported yet.
            new ArrayList<String>()
        );
        String config = ldargs.generate();
        
        Output.log("launch.cfg", "Generated config: " + config);

        // Eventually generate the config and copy the launcher file in.
        TextFile appldr_args;
        try {
            Path lf = new Path("launcher_bin.jar");
            lf.copy(path + "\\" + args.jarName + ".jar");
            
            appldr_args = new TextFile(path + "\\apploader_args.txt", "w");
            appldr_args.write(config);
            appldr_args.close();
        }
        catch (IOException ie) {
            ie.printStackTrace();
            Output.critical("cfg.write", "Access to config file failed.");
            Common.fatalError("Could not write launch config.", "cfg.write");
        }
    }*/
}
