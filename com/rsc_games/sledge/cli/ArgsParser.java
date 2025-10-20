package com.rsc_games.sledge.cli;

import java.util.HashMap;
import java.util.HashSet;

public class ArgsParser {
    static final HashSet<String> legalSwitches;
    static final HashSet<String> legalFlags;

    static {
        legalSwitches = new HashSet<String>();

        legalSwitches.add("verbose");
        legalSwitches.add("hammer");
        legalSwitches.add("help");

        legalFlags = new HashSet<String>();

        legalFlags.add("D");
        legalFlags.add("v");
        legalFlags.add("c");
        legalFlags.add("h");
    }

    /**
     * User-provided command line options to be forwarded to the script.
     */
    HashMap<String, String> options = new HashMap<String, String>();

    /**
     * Switches intended for sledge itself and not the script. Also contains
     * non-option flags.
     */
    HashMap<String, String> switches = new HashMap<String, String>();

    /**
     * Build target to execute (within the executable tree)
     * 
     * init is aliased to .init
     * fullclean is aliased to .purge
     */
    String target;

    // /**
    //  * 
    //  */
    // int argsCount = 0;

    /**
     * Parse the given arguments and provide them in a form intelligible
     * to the rest of the build system.
     * 
     * @param args Input command line arguments.
     */
    public ArgsParser(String[] args) {
        this.target = parseTarget(args);
        parseCommandLine(args);
    }

    /**
     * Parse the target from the full command line.
     * 
     * @param args Copy of the args list passed in above
     * @return The target string, if any was provided.
     */
    private String parseTarget(String[] args) {
        String foundTarget = null;

        for (String arg : args) {
            // Flag/switch; not a target
            if (arg.charAt(0) == '-') 
                continue;

            if (foundTarget != null) 
                throw new IllegalArgumentException("Too many targets specified.");

            foundTarget = arg;
        }

        return foundTarget;
    }

    /**
     * Build an easily-referencable table of the provided flags.
     * 
     * @param args Input args list
     * @return A list of the pass through options (ignores switches)
     */
    private HashMap<String, String> parseCommandLine(String[] args) {
        HashMap<String, String> foundOptions = new HashMap<String, String>();

        for (String arg : args) {
            // Some other command line switch.
            if (arg.startsWith("--"))
                processSwitch(arg.substring(2));

            // Short length switches.
            else if (arg.charAt(0) == '-')
                processFlags(arg.substring(1));

            // Probably the target. Skip.
        }

        return foundOptions;
    }

    /**
     * Process a long-form switch (--<switch-name>).
     * 
     * @param arg The arg to parse
     * @return The key-value pair (if any)
     */
    private void processSwitch(String arg) {
        String[] keyValuePair = getKeyValuePair(arg);

        if (!legalSwitches.contains(keyValuePair[0]))
            throw new ArgsParseException("unrecognized switch", "--" + keyValuePair[0]); 

        this.switches.put(keyValuePair[0], keyValuePair[1]);
    }

    /**
     * Parse the option into a key-value pair.
     * 
     * @param arg Option to parse (in the full BLAH_BLAH form, without -D)
     * @return The parsed string as an array in the form (key, value)
     */
    private void processFlags(String arg) {
        String optionCode = arg.substring(0, 1);

        // Handle all other flag types (like -fLaGs). Each is parsed individually.
        // TODO: short-length args don't parse arguments properly.
        if (!optionCode.equals("D")) {
            for (char option : arg.toCharArray()) {
                if (!legalFlags.contains("" + option))
                    throw new ArgsParseException("unrecognized flag", "-" + optionCode);

                switches.put("" + option, null);
            }

            return;
        }

        // Process the sledge variable (in the format of -DVARIABLE)
        String[] var = getKeyValuePair(arg.substring(1));
        options.put(var[0], var[1]);
    }

    /**
     * Extract a key/value pair from the given arg.
     * 
     * @param arg The key/value pair in the command line.
     * @return The split key/value.
     */
    private String[] getKeyValuePair(String arg) {
        // Parse the key and value, if any.
        int eqIndex = arg.indexOf("=");

        if (eqIndex == -1) 
            return new String[] {arg, null};

        // Only split the key/value pair in half. Don't recursively do it.
        return arg.split("=", 2);
    }

    public String getTarget() {
        return this.target;
    }

    /**
     * Get the full list of sledge variables to pass in.
     * 
     * @return
     */
    public HashMap<String, String> getOptions() {
        return this.options;
    }

    public HashMap<String, String> getSwitches() {
        return this.switches;
    }
}