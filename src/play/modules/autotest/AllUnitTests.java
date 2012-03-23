package play.modules.autotest;

import org.junit.AfterClass;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import play.Play;
import play.test.PlayJUnitRunner;
import play.test.TestEngine;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@RunWith(AllUnitTests.class)
public class AllUnitTests extends ParentRunner<Runner> {

	private List<Runner> runners;

	public AllUnitTests(Class<?> testClass) throws Exception {
		super(testClass);
		if (!Play.started) {
            Play.init(new File("."), "test");
            Play.javaPath.add(Play.getVirtualFile("test"));
            Play.start();
		}
		
		List<Class> tests = TestEngine.allUnitTests();
		runners = new ArrayList<Runner>();
		for (Class test : tests) {
			PlayJUnitRunner runner = new PlayJUnitRunner(test);
            PlayJUnitRunner.useCustomRunner = true;
			runners.add(runner);
		}
	}

	@Override
	protected Description describeChild(Runner runner) {
		return runner.getDescription();
	}

	@Override
	protected List<Runner> getChildren() {
		return runners;
	}

	@Override
	protected void runChild(Runner runner, RunNotifier notifier) {
		runner.run(notifier);
	}

	@AfterClass
	public static void stopPlay() {
		Play.stop();
	}
}
