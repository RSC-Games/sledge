package com.rsc_games.sledge.cli;

import java.util.HashMap;
import java.util.HashSet;

import com.rsc_games.sledge.lib.LogModule;

public class ArgsParser {
    static final HashSet<String> legalSwitches;
    static final HashSet<String> legalFlags;
    static final HashSet<String> optionsWithArgs;

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

        // Some options mandate arguments to function properly.
        // For proper error detection, long form swtches will be included
        // in here as well.
        optionsWithArgs = new HashSet<String>();

        optionsWithArgs.add("hammer");
        optionsWithArgs.add("D"); // Not necessary since it has special parsing logic.
        optionsWithArgs.add("c");
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
    private ArgsParser(String[] args) {
        parseCommandLine(args);
    }

    /**
     * Parse the given arguments and provide them in a form intelligible
     * to the rest of the build system. Factory wrapper around the constructor
     * to hide the exception details and print them for the end user.
     * 
     * @param args Input command line arguments.
     */
    public static ArgsParser processCommandLineArgs(String[] args) {
        try {
            return new ArgsParser(args);
        }
        catch (ArgsParseException ie) {
            LogModule.error("sledge", String.format("fatal: %s: %s", ie.getMessage(), ie.getFaultingFlag()));
            return null;
        }
    }

    /**
     * Build an easily-referencable table of the provided flags.
     * 
     * @param args Input args list
     * @return A list of the pass through options (ignores switches)
     */
    private HashMap<String, String> parseCommandLine(String[] args) {
        HashMap<String, String> foundOptions = new HashMap<String, String>();

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            String possibleParam = i < args.length - 1 ? args[i + 1] : null; 

            // Long form command line option (args must be specified with the form
            // option=data)
            if (arg.startsWith("--"))
                processSwitch(arg.substring(2));

            // Short length switches. Can parse the next argument in the list
            // if the short-length option needs an argument.
            else if (arg.charAt(0) == '-') {
                boolean consumedArg = processFlags(arg.substring(1), possibleParam);

                if (consumedArg) {
                    if (possibleParam == null)
                        throw new ArgsParseException("missing argument", possibleParam);

                    // Next element already processed. Skip.
                    i++;
                }
            }

            // Probably the target.
            else {
                if (target != null)
                    throw new ArgsParseException("multiple targets specified", "");

                this.target = arg;
            }

        }

        return foundOptions;
    }

    /**
     * Process a long-form switch (--<switch-name>).
     * 
     * @param arg The arg to parse
     */
    private void processSwitch(String arg) {
        String[] keyValuePair = getKeyValuePair(arg);
        String cliSwitch = keyValuePair[0];

        if (!legalSwitches.contains(cliSwitch))
            throw new ArgsParseException("unrecognized switch", "--" + cliSwitch); 

        if (optionsWithArgs.contains(cliSwitch) && keyValuePair[1] == null)
            throw new ArgsParseException("missing arg for switch", cliSwitch);

        this.switches.put(cliSwitch, keyValuePair[1]);
    }

    /**
     * Parse the option into a key-value pair.
     * 
     * @param arg Option to parse (in the full BLAH_BLAH form, without -D)
     * @return Whether the next argument in the argslist should be skipped
     *  since it's part of this flag.
     */
    private boolean processFlags(String arg, String potentialParam) {
        String optionCode = arg.substring(0, 1);

        // Handle all other flag types (like -fLaGs). Each is parsed individually.
        if (!optionCode.equals("D")) {
            for (char option : arg.toCharArray()) {
                if (!legalFlags.contains("" + option))
                    throw new ArgsParseException("unrecognized flag", "-" + optionCode);

                if (optionsWithArgs.contains("" + option)) {
                    if (arg.length() > 1)
                        throw new ArgsParseException("multiple options specified for arg", potentialParam);

                    switches.put("" + option, potentialParam);
                    return true;
                }
                
                // No argument required. Keep parsing.
                switches.put("" + option, null);
            }

            return false;
        }

        // Process the sledge variable (in the format of -DVARIABLE)
        String[] var = getKeyValuePair(arg.substring(1));
        options.put(var[0], var[1]);
        return false;
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
     * Inform the outer code a help message was requested by the user.
     * 
     * @return Whether the --help or -h flags were passed.
     */
    public boolean helpMessageRequested() {
        return this.switches.containsKey("help") || this.switches.containsKey("h");
    }

    /**
     * Get the full list of sledge variables to pass in.
     * 
     * @return The sledge pass-through options.
     */
    public HashMap<String, String> getOptions() {
        return this.options;
    }

    /**
     * Get a full list of all the long and short form command line flags.
     * 
     * @return The command line switches.
     */
    public HashMap<String, String> getSwitches() {
        return this.switches;
    }
}