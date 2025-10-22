// package modules.jdkutil;

// import java.io.IOException;
// import java.util.Map;

// import com.rsc_games.sledge.lib.LogModule;

// import java.io.File;
// import util.ProcessIO;
// import util.TextFile;
// import common.VariableState;
// import common.Environ;
// import common.Common;
// import common.Path;

// public class JavaCompileSystem {
//     public static int build(Environ env, int version, String args) {
//         LogModule.log("javac", "Found JDK: ", false); // Don't add a terminating newline.

//         // Query compiler version.
//         String javaPath = env.getJavaPath();
//         int code = execCmd(env, javaPath + "javac -version");
//         if (code != 0) return code;

//         LogModule.log("javac", "Building application on javac host compiler.");

//         // Build the application code.
//         // TODO: FORMER JCOMP V8 IS A SLOPPY SOLUTION TO A PRACTICAL PROBLEM!
//         if (version == 8) {
//             // Currently a workaround for school pcs. Since it's not maintainable, it will
//             // eventually be discarded. For general compilation, use jcomp, not jcompv8.
//             String rtpath = "C:\\Program Files (x86)\\Java\\jre1.8.0_311\\lib\\rt.jar";
//             //LogModule.log("jcomp", "Running Javac " + jdkPath + "/javac -source 8 -target 8 -bootclasspath \""
//             //           + rtpath + "\" " + args);
//             return execCmd(env, javaPath + "javac -source 8 -target 8 -bootclasspath \""
//                            + rtpath + "\" " + args);
//         }

//         //LogModule.log("jcomp", "Running Javac " + jdkPath + "/javac " + args);
//         return execCmd(env, javaPath + "javac " + args);
//     }

//     public static int buildLib(Environ env, String args) {
//         LogModule.log("javac_lib", "Found JDK: ", false); // Don't add a terminating newline.

//         // Query compiler version.
//         String javaPath = env.getJavaPath();
//         int code = execCmd(env, javaPath + "javac -version");
//         if (code != 0) return code;

//         LogModule.log("javac_lib", "Building library on javac host compiler.");

//         // Build EVERY SINGLE SOURCE FILE IN THE FOLDER!
//         File cwd = new File(env.getcwd());

//         srcCount = env.getPath().enumFiles(".java");
//         LogModule.log("javac_lib", "Compiling " + srcCount + " source files...");
//         builtFiles = 1;

//         try {
//             buildLib0(env, cwd, javaPath);
//         }
//         catch (RuntimeException ie) {
//             ie.printStackTrace();
//             return 1;
//         }

//         return 0;
//     }

//     // TODO: sloppy
//     static int builtFiles = 1;
//     static int srcCount = 0;
//     private static void buildLib0(Environ env, File cwd, String jdkPath) {
//         for (File entry : cwd.listFiles()) {
//             if (entry.isDirectory()) {
//                 buildLib0(env, entry, jdkPath);
//             }

//             String fname = entry.getAbsolutePath();

//             if (fname.lastIndexOf(".java") != -1) {
//                 // Speedup: Skip java files with an associated .class file that has been already built.
//                 String className = fname.substring(0, fname.lastIndexOf(".")) + ".class";
//                 if (new File(className).exists()) {
//                     LogModule.log("javac_lib", "Skipping Java object file ("  + builtFiles + "/" + srcCount
//                                + ") File path: ..." + fname.substring(Math.max(fname.length() - 48, 0)));
//                     builtFiles++;
//                     continue;
//                 }

//                 // Only add quotes if the path has spaces.
//                 String quotes = fname.indexOf(" ") != -1 ? "\"" : "";

//                 // Build the object.
//                 String cmdline = jdkPath + "javac -cp " + VariableState.get("CLASSPATH") 
//                                  + " " + quotes + fname + quotes;
//                 //LogModule.log("javac_lib", "Running Javac " + cmdline);
//                 LogModule.log("javac_lib", "Building Java object file (" + builtFiles + "/" + srcCount 
//                            + ") File path: ..." + fname.substring(Math.max(fname.length() - 48, 0)));

//                 int ret = execCmd(env, cmdline);
//                 builtFiles++;

//                 if (ret != 0) 
//                     throw new RuntimeException("Failed to compile lib file " + entry);
//             }
//         }
//     }

//     // TODO: Stop using varstate to extract the classpath.
//     public static int link(Environ env, String args) {
//         LogModule.log("link", "Generating LogModule binary with provided JAVAC JAR util.");

//         // Generate manifest from the current CLASSPATH var.
//         String cp = VariableState.get("CLASSPATH");
//         String[] argsArr = args.split(" ");
//         cp = reparseClassPath(cp);

//         String manifest = "Manifest-Version: 1.0\nCreated-By: 17.0.2 (Oracle Corporation)\n"
//             +"Application-Name: sledge-generated binary (RSC Games)\nMain-Class: " + argsArr[1]
//             + "\nClass-Path: " + cp + "\n";

//         try {
//             TextFile mf = new TextFile(env.getcwd() + "/gen_mf.txt", "w");
//             mf.write(manifest);
//             mf.close();
//         }
//         catch (IOException ie) {
//             ie.printStackTrace();
//             return -1;
//         }

//         // Build the archive.
//         String jdkpath = env.getJavaPath();
//         String cmdline = "jar --create --file " + argsArr[0] + " --manifest gen_mf.txt .";

//         LogModule.log("link", "Using cmdline " + jdkpath + cmdline);
//         int code = execCmd(env, jdkpath + cmdline);
        
//         // TODO: Remove gen_mf.txt
//         return code;
//     }

//     // NOT IMPLEMENTED!!!!!!!!!!!!!
//     public static int injectLoader(Environ env, String args) {
//         // If using the launcher, generate the launcher config 
//         LogModule.log("linkldr", "Linking loader code...");

//         // Link the binary and the loader code.
//         String binName = VariableState.get("BINARY");
//         String cwd = env.getcwd();
//         File resourceBin = new File(cwd + "/" + binName);
//         File newBin = new File(cwd + "/" + gameToResources(binName));
//         resourceBin.renameTo(newBin);

//         // Inject the loader binary.
//         try {
//             Path ldrbin = new Path(cwd + "/../ldrbin.jar");
//             ldrbin.copy(cwd + "/" + binName);
//         }
//         catch (IOException ie) {
//             ie.printStackTrace();
//             LogModule.critical("linkldr", "Failed to inject loader binary!");
//             return 1;
//         }

//         // Generate the launch arguments.
//         TextFile ldrargs;
//         try {
//             ldrargs = new TextFile(cwd + "/apploader_args.txt", "w");
//             ldrargs.write("1000102, " + gameToResources(binName) + ", NO_UPDATE, " + args +
//                 ", " + env.getJavaPath() + "java.exe, APP_ARGS_0");
//             ldrargs.close();
//         }
//         catch (IOException ie) {
//             ie.printStackTrace();
//             LogModule.critical("linkldr", "Failed to generate args file.");
//             return 2;
//         }

//         // Allow launching via the console.
//         TextFile cmd;
//         try {
//             cmd = new TextFile(cwd + "/run_console.bat", "w");
//             cmd.write("java -jar " + binName + "\npause"); // debugging
//             cmd.close();
//         }
//         catch (IOException ie) {
//             ie.printStackTrace();
//             LogModule.warn("main", "Could not generate console launch args for app! Skipping...");
//         }
        
//         return 0;
//     }

//     static String gameToResources(String name) {
//         String filename = name.substring(0, name.lastIndexOf("."));
//         String ext = name.substring(name.lastIndexOf("."));
//         return filename + "_resources" + ext;
//     }
    
//     static String reparseClassPath(String cp) {
//         String[] splitargs = null;
//         String platform = VariableState.get("PLATFORM");

//         if (platform.equals("linux"))
//             splitargs = cp.split(":");
//         else if (platform.equals("win"))
//             splitargs = cp.split(";");
        
//         return String.join(" ", splitargs);
//     }

//     static int execCmd(Environ env, String cmd) {
//         Runtime rt = Runtime.getRuntime();

//         int code = 4096;
//         try {
//             Map<String, String> envVars = System.getenv();
//             String[] arrEnv = new String[envVars.size()];

//             int i = 0;
//             for (String key : envVars.keySet()) {
//                 String value = envVars.get(key);

//                 if (key.equals("PWD"))
//                     value = env.getcwd();

//                 arrEnv[i++] = key + "=" + value;
//             }

//             final Process proc = rt.exec(cmd, arrEnv, new File(env.getcwd()));
//             ProcessIO.startAsyncReadPipe(proc);
//             code = proc.waitFor();
//             Common.sleep(500);
//             ProcessIO.stopReadPipe();
//         }
//         catch (InterruptedException ie) {}
//         catch (IOException ie) {
//             ie.printStackTrace();
//         }
        
//         return code;
//     }
// }