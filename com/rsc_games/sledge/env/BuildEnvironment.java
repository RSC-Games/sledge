package com.rsc_games.sledge.env;

import java.util.ArrayList;

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
    // TODO: add command line flag to change the config file to use.
    private final String configFile = "./hammer";

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
     * Load all of these project metadata fields from the metadata file stored in the
     * build folder. This will be automatically created by ``sledge init``.
     * 
     * @param projectMetaFile
     */
    public BuildEnvironment(String projectMetaFile) {
        this.builderVars = new BuilderVars();

        // TODO: Load the project data json and populate the fields.
    }

    public String getConfigFilePath() {
        return this.configFile;
    }

    public BuilderVars getVars() {
        return this.builderVars;
    }
}
