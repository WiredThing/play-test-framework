package play.modules.autotest;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import play.Play;

import java.util.Properties;

@SuppressWarnings("UnusedDeclaration")
@RunWith(JUnit4.class)
public abstract class MicroTest {
	protected Mockery context;
	
	@Before
	public void createContext() {
		context = new JUnit4Mockery();
		context.setImposteriser(ClassImposteriser.INSTANCE);
		Play.configuration = new Properties();
	}
	
	protected <T> T mock(Class<T> clazz) {
		return context.mock(clazz);
	}
	
	protected <T> T mock(Class<T> clazz, String id) {
		return context.mock(clazz, id);
	}
	
	protected void checking(Expectations expectations) {
		context.checking(expectations);
	}
	
}
