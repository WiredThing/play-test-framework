package play.modules.autotest;

import org.junit.Before;
import play.mvc.Http.Response;
import play.test.Fixtures;
import play.test.FunctionalTest;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("UnusedDeclaration")
public abstract class ControllerTest extends FunctionalTest {

	@Before
	public void setUp() {
		Fixtures.deleteDatabase();
	}
	
	protected Response login(String username, String password) {
		Map<String, String> loginUserParams = new HashMap<String, String>(); 
	    loginUserParams.put("username", username); 
	    loginUserParams.put("password", password);
	    try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// Ignore
		}
	    return POST("/SecureController/authenticate", loginUserParams);
	}

    protected String postJSON(String json, String url) throws Exception {
		Response response = POST(url, "application/json", json);
		return new String(response.out.toByteArray());
	}

    protected String getJSON(String url) {
        Response response = GET(url, true);
        assertStatus(200, response);
        return new String(response.out.toByteArray());
    }
}
