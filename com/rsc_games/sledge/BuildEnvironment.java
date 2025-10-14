package com.rsc_games.sledge;

import java.util.ArrayList;

public class BuildEnvironment {

    /**
     * Command/path for running javac (by default sledge assumes "javac" found on
     * PATH)
     */
    private String javacPath;

    /**
     * Command/path for the jar command (by default sledge assumes "jar" found on
     * PATH)
     */
    private String jarToolPath;

    /**
     * Minimum required javac version for building and deploying this project.
     */
    private int minCompilerVersion;

    /**
     * Target path to store all build artifacts in. Should be a subdirectory of the
     * main project folder.
     */
    private String buildFolder;

    /**
     * Root folder of the project sledge needs to build.
     */
    private String projectFolder;

    /**
     * JAR libraries the generated binary requires to run.
     */
    private ArrayList<String> linkableLibraries;

    /**
     * Load all of these project metadata fields from the metadata file stored in the
     * build folder. This will be automatically created by ``sledge init``.
     * 
     * @param projectMetaFile
     */
    public BuildEnvironment(String projectMetaFile) {
        // TODO: Load the project data json and populate the fields.
        
    }
}
