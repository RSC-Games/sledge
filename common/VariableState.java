package common;

import java.util.HashMap;

/**
 * PART OF JBUILDER VERSION 2! DO NOT PURGE!
 */
public class VariableState {
    public static HashMap<String, String> vars = new HashMap<String, String>();

    public static void append(String var, String value) {
        vars.put(var, vars.get(var) + value);
    }

    public static void set(String var, String value) {
        vars.put(var, value);
    }

    public static void addVars(HashMap<String, String> moreVars) {
        vars.putAll(moreVars);
    }

    public static String get(String var) {
        String v = vars.get(var);
        return v != null ? v : "";
    }

    /**
     * Populate sledge's built-in variables.
     */
    static {
        vars.put("PLATFORM", getOSType());
        vars.put("ARCHITECTURE", System.getProperty("os.arch"));
    }

    private static String getOSType() {
        String os = System.getProperty("os.name");

        switch (os) {
            case "Windows 11":
                return "win";
            case "Windows 10":
                return "win";
            case "Linux":
                return "linux";
            default:
                throw new UnsupportedOperationException("Cannot identify OS " + os);
        }
    }
}
