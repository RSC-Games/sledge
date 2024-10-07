package modules.systemio;

import java.io.IOException;
import common.Environ;
import common.Path;
import util.Output;

public class UpdateBuilder {
    public static void copyTo(Environ env, String args) {
        Output.log("cpjbuild", "Overwriting jBuilder binary with newly generated binary.");
        Path output = new Path(env.getcwd() + "/" + args);

        try {
            output.copy("../jbuilder.jar");
        }
        catch (IOException ie) {
            ie.printStackTrace();
            Output.warn("cpjbuild", "Could not copy file. Skipping...");
        }
    }
}
