package com.rsc_games.sledge.env;

import java.util.HashMap;

import com.rsc_games.sledge.VersionHeader;

public class BuilderVars {
    /**
     * List of all vars defined within the project. Any defined within .init are
     * stored in the project metadata, and some are automatically defined by
     * sledge at startup.
     */
    private HashMap<String, String> vars = new HashMap<String, String>();

    /**
     * Only allow the build environment to create the variable tracker, so we can
     * ensure they're all in one place.
     */
    BuilderVars() {
        createDefaultVars();
    }

    private void createDefaultVars() {
        vars.put("PLATFORM", getOSType());
        vars.put("ARCH", System.getProperty("os.arch"));
        vars.put("SLEDGE_VERSION", "" + VersionHeader.VERSION.v);
        vars.put("SLEDGE_MAJOR", "" + VersionHeader.VERSION.major);
        vars.put("SLEDGE_MINOR", "" + VersionHeader.VERSION.minor);
    }

    /**
     * Provide a predictable os name for each platform, since its value
     * in the os.name environment var will vary by the OS version.
     * 
     * @return The predictable OS name, like windows, linux, or macos.
     */
    private static String getOSType() {
        String os = System.getProperty("os.name");

        switch (os) {
            case "Windows 11":
                return "windows";
            case "Windows 10":
                return "windows";
            case "Linux":
                return "linux";
            case "posix": // Internet says os.name env var is this for Mac OS
                return "macos";
            default:
                throw new UnsupportedOperationException("Cannot identify OS " + os);
        }
    }

    public void append(String var, String value) {
        vars.put(var, vars.get(var) + value);
    }

    public void set(String var, String value) {
        vars.put(var, value);
    }

    public void addVars(HashMap<String, String> moreVars) {
        vars.putAll(moreVars);
    }

    public String get(String var) {
        String v = vars.get(var);
        return v != null ? v : "";
    }

    public boolean exists(String var) {
        return vars.get(var) != null;
    }

    public void printArgs() {
        System.out.println(vars);
    }
}
