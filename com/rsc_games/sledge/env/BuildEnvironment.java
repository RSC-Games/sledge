package com.rsc_games.sledge.env;

import java.util.ArrayList;
import java.util.HashMap;

import com.rsc_games.sledge.cli.ArgsParser;

public class BuildEnvironment {
    /**
     * Command/path for running javac (by default sledge assumes "javac" found on
     * PATH)
     */
    private String javacPath = "javac";

    /**
     * Command/path for the jar command (by default sledge assumes "jar" found on
     * PATH)
     */
    private String jarToolPath = "jar";

    /**
     * Minimum required javac version for building and deploying this project.
     */
    private int minCompilerVersion = 17;

    /**
     * Target path to store all build artifacts in. Should be a subdirectory of the
     * main project folder.
     */
    private String buildFolder = "./build";

    /**
     * Root folder of the project sledge needs to build.
     */
    private String projectFolder = ".";

    /**
     * Path to the file containing all of the build instructions. Due to internal
     * limitations of sledge, this cannot technically be changed.
     */
    private String configFile = "./hammer";

    /**
     * JAR libraries the generated binary requires to run.
     */
    private ArrayList<String> linkableLibraries;

    /**
     * Contains all of the sledge variables- whether they were defined in the project 
     * file or on the command line...
     */
    private BuilderVars builderVars;

    /**
     * Command line switches that were passed into sledge. Does not contain any sledge
     * vars.
     */
    private HashMap<String, String> commandLineSwitches;

    /**
     * Load all of these project metadata fields from the metadata file stored in the
     * build folder. This will be automatically created by ``sledge init``.
     * 
     * @param projectMetaFile
     */
    public BuildEnvironment(ArgsParser argsParser) {
        this.builderVars = new BuilderVars();
        this.builderVars.addVars(argsParser.getOptions());
        this.commandLineSwitches = argsParser.getSwitches();

        // TODO: Load the project data json and populate the fields.
        if (this.switchExists("hammer") /*|| this.switchExists("c")*/) {
            String configPath = this.getSwitch("hammer");

            if (configPath == null)
                // TODO: fatal exception
                throw new RuntimeException("missing value for hammer flag");

            this.configFile = configPath;
        }

    }

    public String getConfigFilePath() {
        return this.configFile;
    }

    public BuilderVars getVars() {
        return this.builderVars;
    }

    /**
     * Get a flag/switch's value. Does not determine whether it exists
     * or not.
     * 
     * @param switchID The switch flag.
     * @return The value of the switch (which can be null).
     */
    public String getSwitch(String switchID) {
        return commandLineSwitches.get(switchID);
    }

    /**
     * Determine whether a flag/switch exists. Most flags don't have values
     * so this is the best way to determine that.
     * 
     * @param switchID The switch flag.
     * @return The value of the switch.
     */
    public boolean switchExists(String switchID) {
        return commandLineSwitches.containsKey(switchID);
    }

    /**
     * Determine if sledge should print verbose output.
     * 
     * @return Whether verbose debugging output is enabled.
     */
    public boolean debuggingEnabled() {
        return this.switchExists("verbose") || this.switchExists("v");
    }
}
