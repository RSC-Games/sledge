package common;

import java.io.File;
import java.net.URISyntaxException;

// Workaround for lack of current working directory support in pure Java.
public class Environ {
    String cwd;
    public final String javaPath;
    public final String srcPath;
    public final String sledgePath;

    public Environ(String cwd, String javaPath) throws URISyntaxException{
        this.cwd = cwd;
        this.javaPath = javaPath;
        this.srcPath = cwd;
        this.sledgePath = new File(Environ.class.getProtectionDomain().getCodeSource().getLocation().toURI()).toString();
        System.out.println(sledgePath);
    }

    public void chdir(String newS) {
        this.cwd += newS; // Strip down and simplify absolute path later.
    }

    public void setdir(String path) {
        this.cwd = path;
    }
    
    public String getJavaPath() {
        return this.javaPath;
    }

    public Path getPath() {
        return new Path(this);
    }

    public int execCmd(String cmd, String args) {
        return -1;
    }

    public String getcwd() {
        return cwd;
    }

    public String toString() {
        return cwd + "\\"; // Allows easier path generation.
    }
}
