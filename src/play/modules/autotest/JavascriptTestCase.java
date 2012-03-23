package play.modules.autotest;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;
import junit.framework.TestResult;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JavascriptTestCase extends TestCase {

    private static final Pattern FAILURE_PATTERN = Pattern.compile(".*(\\d+) failure.*");

    private String jsToTest;
    private WebDriver driver;

    public JavascriptTestCase(String jsToTest, WebDriver webDriver) {
        this.jsToTest = jsToTest;
        this.driver = webDriver;
    }

    public TestResult run() {
    	TestResult result = new TestResult();
        driver.get(getBrowserBaseUrl() + "/jstests/testFrame.html?" + jsToTest);

        boolean done = false;
        int attempts = 0;

        while (attempts < 50 && !done) {
            try {
                WebElement element = driver.findElement(By.className("finished-at"));
                if (element.getText() != null && element.getText().length() > 0) {
                    done = true;
                }
            } catch (NoSuchElementException e) {
                try {
					Thread.sleep(100);
				} catch (InterruptedException ie) {
					// Ignore
				}
            }
        }

        if (done) {
            String failures = getFailureCount();
            StringBuilder messages = new StringBuilder();
            if (!done) {
                messages.append("Test timed out");
            }
            messages.append("Failure messages:\n");
            List<String> failureMessages = getFailureMessages();
            for (String failureMessage : failureMessages) {
                messages.append(failureMessage).append("\n");
            }
            try {
            	assertEquals("Javascript test " + jsToTest + ": " + failures + " tests failed:\n" + messages.toString(), "0", failures);
            } catch (AssertionFailedError e) {
            	result.addFailure(this, e);
            }
        } else {
            result.addError(this, new RuntimeException("Failed to complete"));
        }
        
		return result;
    }

    public List<String> getFailureMessages() {
        List<String> messages = new ArrayList<String>();
        List<WebElement> failedTestElements = driver.findElements(By.className("failed"));
        for (WebElement failedTestElement : failedTestElements) {
            if (failedTestElement.getAttribute("class").contains("spec")) {
                WebElement messagesElement = failedTestElement.findElement(By.className("messages"));
                List<WebElement> failures = messagesElement.findElements(By.className("fail"));
                for (WebElement failure : failures) {
                    messages.add(failure.getText());
                }
            }
        }
        return messages;
    }

    public WebElement getResults() {
        return driver.findElement(By.className("jasmine_reporter"));
    }

    public String getFailureCount() {
        WebElement runner = getResults().findElement(By.className("runner"));
        String text = runner.getText();
        if (text.contains("0 failures")) {
            return "0";
        } else {
            String failureLine = runner.findElement(By.cssSelector("a.description")).getText();
            Matcher matcher = FAILURE_PATTERN.matcher(failureLine);
            matcher.find();
            return matcher.group(1);
        }
    }

    private String getBrowserBaseUrl() {
        return "http://" + getAppHostName() + ":9090";
    }

    private String getAppHostName() {
        try {
            return InetAddress.getLocalHost().getCanonicalHostName();
        } catch (UnknownHostException e) {
            System.err.println("Failed to get default host name");
            return "localhost";
        }
    }
}
