package space.edhits.edtrust;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Very simple API key tests
 */
@SpringBootTest
@RunWith(SpringRunner.class)
@ComponentScan
@RestClientTest(ListServiceController.class)
public class BasicTests {

    @Rule
    public ExpectedException expected = ExpectedException.none();

    @Autowired
    private ListServiceController listService;

    @Test
    public void testUnauthorizedApiKey() {
        expected.expect(UnauthorizedApiKey.class);
        ContactRequest contactRequest = new ContactRequest();
        listService.check("invalid api key", contactRequest);
    }
}
