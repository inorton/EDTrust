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
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import space.edhits.edtrust.data.UserProfileData;

/**
 * Very simple API key tests
 */
@SpringBootTest(classes = {
        ListServiceTestConfiguration.class,
        ListServiceConfiguration.class,
        ListServiceController.class})
@RunWith(SpringRunner.class)
@RestClientTest(ListServiceController.class)
public class BasicTests {

    @Rule
    public ExpectedException expected = ExpectedException.none();

    @Autowired
    private ListServiceController listService;

    @Autowired
    private UserProfileData users;

    @Test
    public void testUnauthorizedApiKey() throws UnknownUser {
        expected.expect(UnauthorizedApiKey.class);
        ContactRequest contactRequest = new ContactRequest();
        listService.check("invalid api key", contactRequest);
    }

    @Test
    public void testAuthorizedApiKey() throws UnknownUser {
        ContactRequest contactRequest = new ContactRequest();

        // make sure a random cmdr is not on our list
        contactRequest.setCmdr(TestHelpers.randomString());
        ResponseEntity<ContactResponse> check = listService.check(users.getApiKey(users.getId(TestHelpers.TEST_USER_EMAIL)),
                contactRequest);
    }

}
