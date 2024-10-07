import util.*;
import java.io.IOException;

import modules.Modules;
import modules.wizard.*;

import common.Environ;
import common.Arguments;
import common.VariableState;
import common.parser.BuildConfig;
//import common.*;

public class Main {
    static final Version VERSION = new Version(2, 0, 0, 15);
    static final int ARGS_SZ = 9;

    public static void main(String[] args) {
        Output.log("main", "jBuilder Copyright 2024 RSC Games. All rights reserved.");
        Output.log("main", "jBuilder binary v" + VERSION);
        Input.init();

        /*
        // User needs the interactive setup module for generating the build script.
        if (!builderRunWithArgs(args)) {
            Output.log("main", "jbuilder was run without arguments. Preparing interactive wizard...");
            Wizard.main();
        }
        */
        Output.warn("main", "jBuilder interactive wizard is not implemented.");

        // Load the jBuilder .ini code eventually
        // Set up and initialize jBuilder's module system.
        String jdkpath = "C:\\Program Files (x86)\\jGRASP\\bundled\\java\\bin";
        Environ env = new Environ(System.getProperty("user.dir"), jdkpath);
        Modules.setOperatingEnvironment(env);
        VariableState.init();

        // Parse the supplied arguments.
        Arguments pargs = new Arguments(args);
        String target = pargs.getTarget();
        VariableState.addVars(pargs.getOptions());

        BuildConfig cfg = new BuildConfig("./jbuildfile");  // Yes like make. Get over it.
        cfg.printTarget(target);
        cfg.getExecutableTree().execTarget(target); 

        Output.info("main", "Compilation successful.");
    }

    public static boolean builderRunWithArgs(String[] args) {
        return args.length > 2;
    }
}