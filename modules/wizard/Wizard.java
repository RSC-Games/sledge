package modules.wizard;

import util.*;
import common.*;
import java.io.IOException;

public class Wizard {
    public static Version WIZARD_VERSION = new Version(1, 1, 0, 0);

    public static void main() {
        Output.log("wizard", "Loaded wizard (v" + WIZARD_VERSION + "). Running interactive session.\n");
        
        System.out.println("No build environment was selected. Would you like to create one? (y/n)");
        if (!Input.input("> ").equals("y")) {
            System.out.println("Please re-run jbuilder with a generated build script!");
            Input.input("Press enter to continue... ");
            System.exit(0);
        }

        System.out.println("What Java Runtime version are you compiling for? 8 or 17 (recommended)?");
        System.out.print("> ");
        int jver = Input.getInt();

        System.out.println("Use jGRASP class extensions? (not recommended) (y/n)");
        boolean enExt = Input.input("> ").equals("y");

        System.out.println("What is the name of your project folder?");
        String projectPath = Input.input("> ");

        System.out.println("What is the name of your main class? (Main is default)");
        String mainClassName = Input.input("> ");

        System.out.println("Would you like to trim down your build size? (y/n) (not recommended)");
        boolean finalStrip = Input.input("> ").equals("y");

        System.out.println("What would you like your output file to be named? (xxx) (do not include the .jar extension)");
        String outputFile = Input.input("> ");

        System.out.println("Would you like to use the RSC Games Java launcher? (y/n) (recommended)");
        boolean useLauncher = Input.input("> ").equals("y");

        System.out.println("Would you like to copy the build into the current folder? (y/n) (not recommended)");
        boolean copyOutput = Input.input("> ").equals("y");

        // Compress all of these choices and generate the jbuild launch batch file.
        String batFileName = "jbuild_" + projectPath + "_jre" + jver + ".bat";
        Output.log("wizard", "Storing build configuration into " + batFileName);
        String[] args = new String[] {
            "" + jver,          // 0
            "" + enExt,         // 1
            projectPath,        // 2
            "" + finalStrip,    // 3
            mainClassName,      // 4
            outputFile,         // 5
            "" + useLauncher,   // 6
            "" + copyOutput     // 7
        };

        writeArgs(batFileName, args);

        Output.log("wizard", "To build this project, double click on " + batFileName);
        Output.log("wizard", "jbuilder will now close.");
        Input.input("Press enter to continue.");
        System.exit(0);
    }

    // Create the launcher batch file used for easy building.
    private static void writeArgs(String batFileName, String[] args) {
        TextFile launchBat = null;
        BuildArgs jArgs = new BuildArgs(args);

        try {
            launchBat = new TextFile(batFileName, "w");
            launchBat.write(jArgs.generate());
        }
        catch (IOException ie) {
            Output.error("wizard.genArgs", "Unable to set build configuration!");
            Common.fatalError("Unable to set build configuration.", "wizard");
        }
        finally {
            if (launchBat != null)
                launchBat.close();
        }
    }
}
