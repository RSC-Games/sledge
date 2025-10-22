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
import com.rsc_games.sledge.parser.MissingTargetException;
import com.rsc_games.sledge.parser.ProcessingException;
import com.rsc_games.sledge.parser.TargetTree;

public class Main {
    // NOTE: Currently in heavy development. Major version will be bumped after this.
    // See Sledge config to change the version string.

    public static void main(String[] args) {
        ArgsParser cliArgs = ArgsParser.processCommandLineArgs(args);

        // Some error happened within the parser.
        if (cliArgs == null)
            System.exit(2);

        String buildTarget = cliArgs.getTarget();

        if (cliArgs.helpMessageRequested()) {
            printHelpMessage();
            return;
        }

        // TODO: Construct build environment, and figure out what to do if no environment exists (probably just
        // default environment, and force run __init?)
        BuildEnvironment buildEnvironment = new BuildEnvironment(cliArgs);
        buildEnvironment.getVars().addVars(cliArgs.getOptions());
        buildEnvironment.getVars().printArgs();

            // Splash ribbon.
        LogModule.log("sledge", "sledge binary " + VersionHeader.VERSION + VersionHeader.SUFFIX);
        LogModule.log("sledge", "sledge is licensed under the Apache License Version 2.0");

        // It doesn't take a genius to figure out how the config file got its name.
        TargetTree targetTree = loadTargetTree(buildEnvironment);

        // Some kind of tree processing error occurred.
        if (targetTree == null) {
            LogModule.error("sledge", String.format("failed to process config file %s. stop.", 
                                                         buildEnvironment.getConfigFilePath()));
            System.exit(3);
        }

        // Sledge can't (reliably) assume a target. Bail out.
        if (buildTarget == null) {
            LogModule.error("sledge", "no target specified. stop.");
            targetTree.listTargets();
            System.exit(2);
        }

        try {
            if (buildEnvironment.debuggingEnabled())
                targetTree.printTarget(buildTarget);

            targetTree.execTarget(buildEnvironment, buildTarget);
        }
        catch (ProcessingException ie) {
            handleParserException(buildEnvironment, ie);
        }
        catch (MissingTargetException ie) {
            LogModule.error("sledge", String.format("%s: %s", ie.getMessage(), ie.getTarget()));
            targetTree.listTargets();
            System.exit(2);
        }
        catch (RuntimeException ie) {
            LogModule.critical("sledge", "internal fatal error! printing backtrace:");
            System.out.print("Exception in thread \"main\" ");
            ie.printStackTrace();

            LogModule.error("sledge", "fatal exception while executing target. stop.");
            System.exit(-4096);
        }
    }

    private static TargetTree loadTargetTree(BuildEnvironment buildEnvironment) {
        try {
            return new TargetTree(buildEnvironment.getConfigFilePath());
        }
        catch (IOException ie) {
            LogModule.error("sledge", String.format("failed to find config file %s. stop.", 
                                                         buildEnvironment.getConfigFilePath()));
            System.exit(2);
        }
        // Failed to process the unit. 
        catch (ProcessingException ie) {
            handleParserException(buildEnvironment, ie);
        }

        return null;
    }

    /**
     * Print a help message with a list of options that sledge understands.
     */
    private static void printHelpMessage() {
        System.out.println("Usage: sledge [options] target [args...]");
        System.out.println("Any arguments prefixed with -D are passed through to the executed sledge");
        System.out.println("script. All others are parsed by sledge itself.");
        System.out.println();
        System.out.println("Where options include:");
        System.out.println();
        System.out.println("\t-v | --verbose");
        System.out.println("\t\tPrint extra debugging information to help debug sledge");
        System.out.println("\t\tscripts or the sledge parser itself.");
        System.out.println("\t-c <hammer file> | --hammer=<hammer file>");
        System.out.println("\t\tUse a config file at a non-standard path (not recommended)");
        System.out.println("\t\tDefaults to ./hammer in the current directory.");
        System.out.println("\t-h | --help");
        System.out.println("\t\tPrints this message.");
        System.out.println("\t-D<key>=<value>");
        System.out.println("\t\tPasses in a key/value pair to the sledge script with the");
        System.out.println("\t\tspecified values. If the value is defined implicitly by");
        System.out.println("\t\tsledge, it can be overridden with this value.");
        System.out.println();
        System.out.println("Short options with arguments can be specified with -<o> <value>, but long");
        System.out.println("options with arguments must be specified with --<option>=<value>.");
        System.out.println();
        System.out.println("For help with sledge's internal units, see the documentation online on the");
        System.out.println("GitHub repository (https://github.com/RSC-Games/sledge.git).");
        System.out.println();
    }

    private static void handleParserException(BuildEnvironment environment, ProcessingException ie) {
        // Only show debugging information if requested.
        if (environment.debuggingEnabled()) {
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
