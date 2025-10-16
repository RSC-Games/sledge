/**
 * Copyright 2024-2025 RSC Games, Raine Bond
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
 * 
 * Unless otherwise specified, all other files within this repository
 * are also licensed under the above License.
 */

package com.rsc_games.sledge;

import java.io.IOException;

import com.rsc_games.sledge.cli.ArgsParser;
import com.rsc_games.sledge.env.BuildEnvironment;
import com.rsc_games.sledge.lib.LogModule;
import com.rsc_games.sledge.parser.ProcessingException;
import com.rsc_games.sledge.parser.TargetTree;

public class Main {
    // NOTE: Currently in heavy development. Major version will be bumped after this.
    // See Sledge config to change the version string.

    public static void main(String[] args) {
        LogModule.log("sledge", "sledge binary " + VersionHeader.VERSION + VersionHeader.SUFFIX);
        LogModule.log("sledge", "sledge is licensed under the Apache License Version 2.0");

        ArgsParser cliArgs = new ArgsParser(args);
        String buildTarget = cliArgs.getTarget();

        // TODO: Construct build environment, and figure out what to do if no environment exists (probably just
        // default environment, and force run __init?)
        BuildEnvironment buildEnvironment = new BuildEnvironment("./build/sledge_project_metadata.json");
        buildEnvironment.getVars().addVars(cliArgs.getOptions());
        buildEnvironment.getVars().printArgs();

        // It doesn't take a genius to figure out how the config file got its name.
        TargetTree targetTree = loadTargetTree(buildEnvironment);

        // Some kind of tree processing error occurred.
        if (targetTree == null) {
            LogModule.error("sledge", "Failed to process ./hammer. Stop.");
            System.exit(1);
        }

        // Sledge can't (reliably) assume a target. Bail out.
        if (buildTarget == null) {
            LogModule.error("sledge", "No target specified. Stop.");
            targetTree.listTargets();
            System.exit(1);
        }
        
        LogModule.log("sledge", "Building target " + buildTarget);

        try {
            if (buildEnvironment.getVars().exists("SLEDGE_DEBUG"))
                targetTree.printTarget(buildTarget);

            targetTree.execTarget(buildEnvironment, buildTarget);
        }
        catch (ProcessingException ie) {
            handleParserException(buildEnvironment, ie);
        }
    }

    private static TargetTree loadTargetTree(BuildEnvironment buildEnvironment) {
        try {
            return new TargetTree(buildEnvironment.getConfigFilePath());
        }
        catch (IOException ie) {
            LogModule.error("sledge", "Failed to find ./hammer. Stop.");
            System.exit(1);
        }
        // Failed to process the unit. 
        catch (ProcessingException ie) {
            handleParserException(buildEnvironment, ie);
        }

        return null;
    }

    private static void handleParserException(BuildEnvironment environment, ProcessingException ie) {
        // Only show debugging information if requested.
        if (environment.getVars().exists("SLEDGE_DEBUG")) {
            LogModule.warn("sledge", "dumping stack trace");
            ie.printStackTrace();
        }

        LogModule.error("sledge", ie.getMessage());
        LogModule.warn("sledge", String.format(
            "at line %d:\n\t> %s", 
            ie.getLineNumber(), 
            TargetTree.getCodeAtLine(environment.getConfigFilePath(), ie.getLineNumber())
        ));
    }
}
