import java.io.File;
import java.io.IOException;

public class RunTests {
    public static void main(String[] args) {
        File testsFolder = new File("./test/");

        File[] tests = testsFolder.listFiles();

        try {
            for (File test : tests) {
                runTest(test);
            }
        }
        catch (TestSuiteFailedException ie) {
            System.err.println("#################################### SOME TEST CASES FAILED ####################################\n");
            System.exit(ie.getReturnValue());
        }
        catch (IOException ie) {
            ie.printStackTrace();
        }
        catch (InterruptedException ie) {
            ie.printStackTrace();
        }
    }

    private static void runTest(File testFile) throws IOException, InterruptedException {
        // TODO: Add support for processing test cases.
        // Currently only supports testing the parser.
        String testCommand = "java -jar ../sledge.jar -c %s test";

        // Extract testing information.
        String[] testInfo = testFile.getName().split("_", 3);
        String testType = testInfo[0];
        String isTest = testInfo[1];
        String testName = testInfo[2];

        if (!isTest.equals("test")) {
            System.err.printf("Expected a unit test, got %s\n", isTest);
            throw new TestSuiteFailedException(-1024); // ERR_UNRECOGNIZED_TEST
        }

        // Perform the test and compare to the expected result.
        System.out.printf("Running test case: %s; Expected result: %s... ", testName, testType);
        Process test = Runtime.getRuntime().exec(String.format(testCommand, "\"" + testFile.getPath() + "\""));
        int retCode = test.waitFor();

        switch (testType) {
            case "fail": {
                // Intended failure expected (retcode 1)
                if (retCode != 3) {
                    System.out.println("TEST FAILED!");
                    System.err.printf("Got return code: %s (expected 3)\n", retCode);
                    printVerboseTestOutput(testFile);
                    throw new TestSuiteFailedException(-1); // ERR_TEST_FAILED.
                }

                System.out.println("test passed");
                break;
            }
            case "pass": {
                // Pass expected (retcode 0)
                if (retCode != 0) {
                    System.out.println("TEST FAILED!");
                    System.err.printf("Got return code: %s (expected 0)\n", retCode);
                    printVerboseTestOutput(testFile);
                    throw new TestSuiteFailedException(-1); // ERR_TEST_FAILED.
                }

                System.out.println("test passed");
                break;
            }
            default: {
                System.err.printf("\nUnable to determine test type: %s", testType);
                throw new TestSuiteFailedException(-512); // ERR_UNKNOWN_TEST_TYPE
            }
        }
    }

    private static void printVerboseTestOutput(File testFile) throws IOException, InterruptedException {
        String testCommandVerbose = "java -jar ../sledge.jar -v -c %s test";

        System.err.println("\n##################################### DETAILED OUTPUT BELOW ####################################");
        ProcessBuilder builder = new ProcessBuilder(String.format(testCommandVerbose, testFile).split(" "));
        builder.inheritIO();
        Process verboseProcess = builder.start();
        verboseProcess.waitFor();
    }
}

class TestSuiteFailedException extends RuntimeException {
    int code;
    
    public TestSuiteFailedException(int retcode) {
        this.code = retcode;
    }

    public int getReturnValue() {
        return this.code;
    }
}