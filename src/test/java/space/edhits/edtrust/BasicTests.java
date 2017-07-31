package space.edhits.edtrust;

import org.junit.AfterClass;
import org.junit.Before;
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
import space.edhits.edtrust.data.CmdrList;
import space.edhits.edtrust.data.UserProfileData;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.core.Is.is;

import java.io.IOException;

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
