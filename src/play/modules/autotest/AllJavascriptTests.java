package play.modules.autotest;

import junit.framework.TestFailure;
import junit.framework.TestResult;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import play.Play;
import play.server.Server;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RunWith(AllJavascriptTests.class)
public class AllJavascriptTests extends AllMatchingTestsSuite {

    protected static WebDriver driver;
    
    public AllJavascriptTests(Class<?> testClass, RunnerBuilder builder) throws InitializationError {
		super(JavascriptTestCase.class, testClass, builder);
		runners = buildRunners();
	}	

    @BeforeClass
    public static void start() throws Exception {
        if (!Play.started) {
            Play.init(new File("."), "test");
            Play.javaPath.add(Play.getVirtualFile("test-js"));
            Play.start();

            new Server(new String[] { "--http.port=9090" });
        }
        
        driver = new FirefoxDriver();
        driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
    }

    @AfterClass
    public static void end() throws Exception {
    	if (driver != null) {
    		driver.close();
    	}
        Play.stop();
    }

    protected List<Class<?>> getSuiteClasses() {
    	return new ArrayList<Class<?>>();
    }

    protected List<Runner> buildRunners() {
        List<Runner> runners = new ArrayList<Runner>();
        List<File> testFiles = new UnitTestDirectoryWalker("-test.js").getMatchingFiles("./test-js");
        for (File jsTestFile : testFiles) {
            runners.add(createJavaScriptRunner(getJsClassName(jsTestFile)));
        }
        
        return runners;
    }

    private String getJsClassName(File jsTestFile) {
    	String path = jsTestFile.getPath();
    	path = path.replace("./test-js/tests/", "");
    	path = path.substring(0, path.lastIndexOf(File.separator) + 1);
    	String name = jsTestFile.getName();
        name = name.substring(name.indexOf(File.separator) + 1, name.lastIndexOf("-"));
        return path + name;
    }

    private Runner createJavaScriptRunner(final String jsTestFile) {
        return new Runner() {

            @Override
            public Description getDescription() {
                return Description.createSuiteDescription(jsTestFile);
            }

            @Override
            public void run(RunNotifier notifier) {
                try {
                    notifier.fireTestStarted(getDescription());
                    System.out.println("Running " + jsTestFile);
                    TestResult result = new JavascriptTestCase(jsTestFile, driver).run();
                    if (result.failureCount() > 0) {
                        notifier.fireTestFailure(getFailure(getDescription(), result));
                        Thread.sleep(100);
                    }
                    notifier.fireTestFinished(getDescription());
                } catch (Throwable e) {
                    notifier.fireTestFailure(new Failure(getDescription(), e));
                }
            }

            private Failure getFailure(Description description, TestResult result) {
                StringBuilder buf = new StringBuilder();
                Enumeration<TestFailure> failures = result.failures();
                while (failures.hasMoreElements()) {
                    TestFailure testFailure = failures.nextElement();
                    buf.append(testFailure).append("\n");
                }
                return new Failure(description, new TestFailed(buf.toString()));
            }
        };
    }

    public class TestFailed extends Throwable {
        public TestFailed(String message) {
            super(message);
        }
    }
}