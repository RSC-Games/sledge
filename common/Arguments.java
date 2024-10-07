package common;

import java.util.HashMap;
import util.Output;

public class Arguments {
    String target;
    HashMap<String, String> options = new HashMap<String, String>();
    int argsCount = 0;

    public Arguments(String[] args) {
        this.target = parseTarget(args);
        this.argsCount = 1;
        Output.log("main", "Executing target " + this.target);
        this.options = parseOptions(args);
    }

    private String parseTarget(String[] args) {
        String foundTarget = null;

        for (String arg : args) {
            // Likely an option. Skip.
            if (arg.charAt(0) == '-') continue;
            if (foundTarget != null) throw new IllegalArgumentException("Too many targets specified.");
            foundTarget = arg;
        }
        
        // Must have a valid target to execute.
        if (foundTarget == null) throw new IllegalArgumentException("Got no target.");

        return foundTarget;
    }

    private HashMap<String, String> parseOptions(String[] args) {
        HashMap<String, String> foundOptions = new HashMap<String, String>();

        for (String arg : args) {
            // Likely an option. Skip.
            if (arg.charAt(0) != '-') continue;
            String[] optionData = parseOption(arg);
            foundOptions.put(optionData[0], optionData[1]);
        }

        return foundOptions;
    }

    private String[] parseOption(String arg) {
        // Trim out the leading dash.
        String parseString = arg.substring(1);

        // Option currently must be d.
        char optionCode = parseString.charAt(0);
        if (optionCode != 'D') throw new IllegalArgumentException("Unknown option: -" + optionCode);
        
        // Parse the key and value, if any.
        int eqIndex = parseString.indexOf("=");
        if (eqIndex == -1) return new String[] {parseString.substring(1), "1"};
        return new String[] {parseString.substring(1, eqIndex), parseString.substring(eqIndex + 1)};
    }

    public String getTarget() {
        return this.target;
    }

    public HashMap<String, String> getOptions() {
        return this.options;
    }
}