package modules.systemio;

import java.io.IOException;

import com.rsc_games.sledge.lib.LogModule;

import common.Environ;
import common.Path;

public class UpdateBuilder {
    public static void copyTo(Environ env, String args) {
        LogModule.log("cpjbuild", "Overwriting jBuilder binary with newly generated binary.");
        Path output = new Path(env.getcwd() + "/" + args);

        try {
            output.copy("../jbuilder.jar");
        }
        catch (IOException ie) {
            ie.printStackTrace();
            LogModule.warn("cpjbuild", "Could not copy file. Skipping...");
        }
    }
}
