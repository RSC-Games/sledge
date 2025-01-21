/**
 * Copyright 2024 RSC Games, Raine Bond
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import util.*;

import modules.Modules;

import common.Environ;

import java.net.URISyntaxException;

import common.Arguments;
import common.VariableState;
import common.parser.BuildConfig;


public class Main {
    static final Version VERSION = new Version(3, 0, 0, 3);
    static final int ARGS_SZ = 9;

    public static void main(String[] args) throws URISyntaxException {
        Output.log("main", "sledge binary v" + VERSION);
        Output.log("main", "sledge is licensed under the Apache 2.0 License.");

        // TODO: Load sledge config from .config/sledge/sledge.conf
        // This contains the java binary path and other configs.
        String javaPath = "";

        // Set up and initialize jBuilder's module system.
        //String jdkpath = "C:\\Program Files (x86)\\jGRASP\\bundled\\java\\bin";
        Environ env = new Environ(System.getProperty("user.dir"), javaPath);
        Modules.setOperatingEnvironment(env);

        BuildConfig cfg = new BuildConfig("./hammer");  // Yes like make. Get over it.

        // Determine the build target and compile options.
        Arguments parsed_args = new Arguments(args);
        String target = parsed_args.getTarget();

        // Ensure there's a target to build for.
        if (target.equals("")) {
            Output.error("main", "No target specified.");
            cfg.listTargets();
            System.exit(1);
        }

        // Build the project.
        VariableState.addVars(parsed_args.getOptions());
        cfg.printTarget(target);
        cfg.getExecutableTree().execTarget(target); 

        Output.info("main", "Compilation successful.");
    }

    public static boolean builderRunWithArgs(String[] args) {
        return args.length > 2;
    }
}