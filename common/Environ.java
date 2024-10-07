package common;

// Workaround for lack of current working directory support in pure Java.
public class Environ {
    String cwd;
    public final String jdkpath;
    public final String projPath;

    public Environ(String cwd, String jdkpath) {
        this.cwd = cwd;
        this.jdkpath = jdkpath;
        this.projPath = cwd;
    }

    public void chdir(String newS) {
        this.cwd += newS; // Strip down and simplify absolute path later.
    }

    public void setdir(String path) {
        this.cwd = path;
    }
    
    public String getJDKPath() {
        return this.jdkpath;
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
