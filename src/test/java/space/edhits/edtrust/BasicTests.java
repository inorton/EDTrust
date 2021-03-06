package space.edhits.edtrust;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * Very simple API key tests
 */
@SpringBootTest(classes = {
        ListServiceTestConfiguration.class,
        ListServiceConfiguration.class,
        ListServiceController.class})
@RunWith(SpringRunner.class)
@RestClientTest(ListServiceController.class)
public class BasicTests extends IntegrationTestBase {

    @Test
    public void testUnauthorizedApiKey() throws Exception {
        expected.expect(UnauthorizedApiKey.class);
        ContactRequest contactRequest = new ContactRequest();
        listService.check("invalid api key", contactRequest);
    }

    @Test
    public void testAuthorizedApiKey() throws Exception {
        ResponseEntity<ContactResponse> check;
        ContactRequest contactRequest = new ContactRequest();
        String apikey = users.getApiKey(users.getId(TestHelpers.TEST_USER_EMAIL));

        // make sure a random cmdr is not on our list
        contactRequest.setCmdr(TestHelpers.randomString());
        check = listService.check(apikey, contactRequest);
        assertThat(check.getBody().getStatus(), is(Constants.RESPONSE_STATUS_UNKNOWN));

        // make sure the bad guy is flagged
        contactRequest.setCmdr(TestHelpers.TEST_BAD_CMDR);
        check = listService.check(apikey, contactRequest);
        assertThat(check.getBody().getStatus(), is(Constants.RESPONSE_STATUS_HOSTILE));
    }

}
