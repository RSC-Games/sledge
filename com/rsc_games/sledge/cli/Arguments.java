package com.rsc_games.sledge.cli;

import java.util.HashMap;

public class Arguments {
    /**
     * User-provided command line options. Accessible within the hammer
     * script.
     */
    // TODO: Add builtin vars like -DSLEDGE_DEBUG
    HashMap<String, String> options = new HashMap<String, String>();

    /**
     * Build target to execute (within the executable tree)
     * 
     * init is aliased to .init
     * fullclean is aliased to .purge
     */
    String target;

    /**
     * 
     */
    // TODO: Remove. Unused.
    int argsCount = 0;

    /**
     * Parse the given arguments and provide them in a form intelligible
     * to the rest of the build system.
     * 
     * @param args Input command line arguments.
     */
    public Arguments(String[] args) {
        this.target = parseTarget(args);
        this.options = parseOptions(args);
        this.argsCount = 1;
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
            // Likely an option. Skip.
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
    private HashMap<String, String> parseOptions(String[] args) {
        HashMap<String, String> foundOptions = new HashMap<String, String>();

        for (String arg : args) {
            // Probably the target. Skip.
            // TODO: Add robust handling for command line flags (won't be -D)
            if (arg.charAt(0) != '-') 
                continue;

            String[] optionData = parseOption(arg);
            foundOptions.put(optionData[0], optionData[1]);
        }

        return foundOptions;
    }

    /**
     * Parse the option into a key-value pair.
     * 
     * @param arg Option to parse (in the full DBLAH_BLAH form)
     * @return The parsed string as an array in the form (key, value)
     */
    private String[] parseOption(String arg) {
        // Trim out the leading dash.
        String parseString = arg.substring(1);

        // No other command line switches are currently supported. (Should add --help)
        char optionCode = parseString.charAt(0);

        if (optionCode != 'D') 
            throw new IllegalArgumentException("Unknown option: -" + optionCode);
        
        // Parse the key and value, if any.
        // If no key is present, sledge defaults to an empty string.
        int eqIndex = parseString.indexOf("=");

        if (eqIndex == -1) 
            return new String[] {parseString.substring(1), ""};

        return parseString.split("=");
    }

    public String getTarget() {
        return this.target;
    }

    /**
     * 
     * @return
     */
    // TODO: Should we even allow access to this?
    public HashMap<String, String> getOptions() {
        return this.options;
    }
}