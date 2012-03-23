package play.modules.autotest;

import play.Play;
import play.mvc.Http.Header;
import play.mvc.Http.Request;

import java.lang.reflect.Field;
import java.util.Properties;

@SuppressWarnings("UnusedDeclaration")
public abstract class PlayMicroTestCase extends MicroTest {

	static {
		Play.configuration = new Properties();
	}
	
	protected void createRequestAndSetOn(Class clazz, String... headers) throws Exception {
		Request request = createRequest(headers);
		
		Field requestField = getField(clazz, "request");
		requestField.setAccessible(true);
		requestField.set(null, request);
	}
	
	protected Request createRequest(String... headers) throws Exception {
		Request request = new Request();
		
		if (headers.length % 2 != 0) {
			throw new IllegalArgumentException("Headers must be specified in pairs");
		}
		
		for (int i = 0; i < headers.length; i+=2) {
			request.headers.put(headers[i], new Header(headers[i], headers[i+1]));
		}
		
		return request;
	}
	
	private Field getField(Class clazz, String fieldName) throws NoSuchFieldException {
		try {
			return clazz.getDeclaredField("request");
		} catch (NoSuchFieldException e) {
			Class superClass = clazz.getSuperclass();
			if (superClass == null) {
				throw e;
			} else {
				return getField(superClass, fieldName);
			}
		}
	}
}
