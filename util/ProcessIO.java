package util;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Scanner;
import java.util.NoSuchElementException;

// Really just a wrapper for I/O between classes. Ideally should be an object but
// right now is just a module.
public class ProcessIO {
    static Scanner s;
    static boolean runReadPipe = false;

    public static void init() {
        s = new Scanner(System.in);
    }

    public static void startAsyncReadPipe(final Process process) {
        runReadPipe = true;
        new Thread() {
            public void run() {
                InputStreamReader error = new InputStreamReader(process.getErrorStream());
                InputStreamReader input = new InputStreamReader(process.getInputStream());
                String line = null; 

                while (runReadPipe) {
                    try {
                        boolean inputStreamAvailable = input.ready() && (line = "" + (char)input.read()) != null;
                        boolean errorStreamAvailable = error.ready() && (line = "" + (char)error.read()) != null;
                        if (inputStreamAvailable || errorStreamAvailable) {
                            System.out.print(line);
                        }
                    }
                    catch (IOException ie) {
                        Output.log("read", "Application died unexpectedly.");
                        try {
                            input.close();
                            error.close();
                        }
                        catch (IOException aie) {}
                        return;
                    }
                }
                //Output.log("read", "Pipe closed.");
            }
        }.start();
    }

    public static void stopReadPipe() {
        runReadPipe = false;
    }

    public static void startAsyncWritePipe(final Process process) {
        new Thread() {
            public void run() {
                OutputStream output = process.getOutputStream();
                while (true) {
                    try {
                        String userInput = s.nextLine();
                        writeChars(output, userInput);
                    }
                    catch (java.nio.BufferOverflowException ie) {
                        return;
                    }
                    catch (IOException ie) {
                        System.out.println("[ldr.run:write]: Application died unexpectedly. Aborting.");
                        try {
                            output.close();
                        }
                        catch (IOException aie) {}
                        return;
                    }
                    catch (NoSuchElementException ie) { // CTRL+C pressed.
                        System.out.println("[ldr.run:write]: Keyboard interrupt received; terminating app.");
                        return;
                    }
                }
            }
        }.start();
    }

    private static void writeChars(OutputStream output, String chars) throws IOException {
        for (int i = 0; i < chars.length(); i++) {
            output.write(chars.charAt(i));
        }
        output.write('\n');
        output.flush();
    }
}
