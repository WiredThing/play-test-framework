package play.modules.autotest;

import javassist.Modifier;
import org.apache.commons.io.DirectoryWalker;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.HiddenFileFilter;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class AllMatchingTestsSuite extends ParentRunner<Runner> {

    protected List<Runner> runners;
	private Class typeClass;
	
	protected AllMatchingTestsSuite(Class<?> testClass) throws InitializationError {
		super(testClass);
	}
	
	public AllMatchingTestsSuite(Class typeClass, Class<?> testClass, RunnerBuilder builder) throws InitializationError {
		this(testClass);
		this.typeClass = typeClass;
        List<Class<?>> testClasses = getSuiteClasses();
		runners = builder.runners(testClass, testClasses.toArray(new Class<?>[testClasses.size()]));
	}
	
	@Override
	protected Description describeChild(Runner child) {
		return child.getDescription();
	}

	@Override
	protected List<Runner> getChildren() {
		return runners;
	}

	@Override
	protected void runChild(Runner runner, RunNotifier notifier) {
		runner.run(notifier);
	}

	protected List<Class<?>> getSuiteClasses() {
		List<Class<?>> testClasses = new ArrayList<Class<?>>();
		
		List<File> testFiles = new UnitTestDirectoryWalker(".java").getMatchingFiles("./test");
		for (File f : testFiles) {
			String path = f.getPath();
			try {
				Class testClass = Class.forName(path.replace("." + File.separator + "test" + File.separator, "").replaceAll(".java", "").replace(File.separator, "."));
				if (isTestCase(testClass)) {
					testClasses.add(testClass);
				}
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		
		return testClasses;
	}
	
	private boolean isTestCase(Class testClass) {
		return typeClass.isAssignableFrom(testClass) && !Modifier.isAbstract(testClass.getModifiers());
	}

	static class UnitTestDirectoryWalker extends DirectoryWalker<File> {
		
		public UnitTestDirectoryWalker(String extension) {
			super(HiddenFileFilter.VISIBLE, FileFilterUtils.suffixFileFilter(extension), -1);
		}
		
		public List<File> getMatchingFiles(String dir) {
			List<File> testFiles = new ArrayList<File>();
			try {
				walk(new File(dir), testFiles);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return testFiles;
		}
		
		protected void handleFile(File file, int depth, Collection<File> results) {
			results.add(file);
		}
	}
}
