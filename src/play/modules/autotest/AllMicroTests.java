package play.modules.autotest;

import org.junit.runner.RunWith;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;

@RunWith(AllMicroTests.class)
public class AllMicroTests extends AllMatchingTestsSuite {
	
	public AllMicroTests(Class<?> testClass, RunnerBuilder builder) throws InitializationError {
		super(MicroTest.class, testClass, builder);
	}	
}
