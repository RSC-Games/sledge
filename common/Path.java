package common;

//import java.nio.*;
import java.nio.file.*;
import java.io.*;
import java.util.ArrayList;

// Use java nio to do this.
@Deprecated
public class Path {
    String p;

    public Path(String path) {
        this.p = path;
    }

    public Path(Environ e) {
        this.p = e.getcwd();
    }

    public void copy(String dest) throws IOException, FileNotFoundException {
        FileInputStream in = new FileInputStream(this.p);
        FileOutputStream out = new FileOutputStream(dest);

        int c;
        while ((c = in.read()) != -1) {
            out.write((char)c);
        }

        in.close();
        out.close();
    }

    public void copy(File inf, File outf) throws IOException, FileNotFoundException {
        Files.copy(inf.toPath(), outf.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    public ArrayList<File> walk() {
        return walk0(getFileHandle(this.p));
    }

    private ArrayList<File> walk0(File folder) {
        ArrayList<File> inodes = new ArrayList<File>();

        for (final File entry : folder.listFiles()) {
            if (entry != null && entry.isDirectory()) {
                inodes.add(entry);
                System.out.println(entry + " is a directory with files!");
                inodes.addAll(walk0(entry));
            }
            else {
                System.out.println("Found file " + entry);
                inodes.add(entry);
            }
        }

        return inodes;
    }

    public void stripFileExt(String ext) {
        stripFileExt0(getFileHandle(this.p), ext);
    }

    private void stripFileExt0(File folder, String ext) {
        for (final File entry : folder.listFiles()) {
            if (entry != null && entry.isDirectory()) {
                stripFileExt0(entry, ext);

                if (entry.listFiles().length == 0)
                    entry.delete();
            }
            else if (entry.getName().lastIndexOf(ext) != -1) {
                entry.delete();
            }
        }
    }

    public int enumFiles(String ext) {
        return enumFiles0(getFileHandle(this.p), ext);
    }

    private int enumFiles0(File folder, String ext) {
        int cnt = 0;

        for (final File entry : folder.listFiles()) {
            if (entry != null && entry.isDirectory()) {
                cnt += enumFiles0(entry, ext);                    
            }
            else if (entry.getName().lastIndexOf(ext) != -1) { // keep the jar
                //System.out.println("discovered " + entry);
                cnt++;
            }
        }

        return cnt;
    }

    public void stripAllButJar() {
        stripToJar0(getFileHandle(this.p));
    }

    private void stripToJar0(File folder) {
        for (final File entry : folder.listFiles()) {
            if (entry != null && entry.isDirectory()) {
                stripToJar0(entry);

                if (entry.listFiles().length == 0)
                    entry.delete();
            }
            else if (entry.getName().lastIndexOf(".jar") == -1) { // keep the jar
                entry.delete();
            }
        }
    }

    public void recursiveDelete() {
        recDelete0(getFileHandle(this.p));
    }

    private void recDelete0(File folder) {
        for (final File entry : folder.listFiles()) {
            if (entry != null && entry.isDirectory())
                recDelete0(entry);

            entry.delete();
        }
    }

    public void copyFrom(String path) throws IOException {
        File outFolder = new File(this.p);
        outFolder.mkdirs();
        copyFrom0(getFileHandle(path), getFileHandle(this.p));
    }

    private void copyFrom0(File src, File dest) throws IOException {
        for (final File entry : src.listFiles()) {
            if (entry != null && entry.isDirectory()) {
                File destFolder = new File(dest, "/" + entry.getName());
                destFolder.mkdirs();
                copyFrom0(entry, destFolder);
                continue;
            }

            File destF = new File(dest, "/" + entry.getName());
            copy(entry, destF);
        }
    }

    private File getFileHandle(String path) {
        return new File(path);
    }

    public String toString() {
        return "Path <" + this.p + ">";
    }
}