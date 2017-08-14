package space.edhits.edtrust;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import space.edhits.edtrust.data.CmdrList;
import space.edhits.edtrust.data.SQLiteCmdrList;
import space.edhits.edtrust.data.SQLiteUserProfileData;
import space.edhits.edtrust.data.UserProfileData;

import java.io.IOException;
import java.util.ArrayList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;


/**
 * Unit tests for user profile data
 */
@RunWith(JUnit4.class)
public class UserProfileDataTests extends TestCommon {

    protected UserProfileData profileData;
    protected CmdrList listData;

    @Before
    public void setupProfileData() {
        listData = new SQLiteCmdrList(testDb);
        profileData = new SQLiteUserProfileData(testDb);
        profileData.init(listData);
    }

    @After
    public void closeDatabase() throws IOException {
        profileData.close();
        listData.close();
    }

    @Test
    public void CreateUserProfile() {
        long userid = makeUser(TestHelpers.randomEmail(), profileData);
        assertThat(userid, greaterThan(new Long(0)));
        String apiKey = profileData.getApiKey(userid);
        assertThat(apiKey, notNullValue());
    }

    @Test
    public void CreateAdminUserProfile() {
        long userid = makeUser(TestHelpers.randomEmail(), profileData);
        assertThat(profileData.getAdminStatus(userid), is(false));
        profileData.updateProfileAdmin(userid, true);
        assertThat(profileData.getAdminStatus(userid), is(true));
    }

    @Test
    public void FindUserByEmail() throws UnknownUser {
        long userid = makeUser(TestHelpers.randomEmail(), profileData);
        assertThat(userid, greaterThan(new Long(0)));
        String email = profileData.getEmail(userid);
        assertThat(email, notNullValue());
        long gotid = profileData.getId(email);
        assertThat(gotid, is(userid));
    }

    @Test
    public void CheckSubscriptions() {
        long userid = makeUser(TestHelpers.randomEmail(), profileData);

        ArrayList<Long> subscriptions = profileData.getActiveSubscriptions(userid);
        assertThat(subscriptions.size(), is(0));

        profileData.activateSubscription(userid, 1);
        profileData.activateSubscription(userid, 2);
        profileData.activateSubscription(userid, 3);
        subscriptions = profileData.getActiveSubscriptions(userid);
        assertThat(subscriptions.size(), is(3));

        profileData.deactivateSubscription(userid, 2);
        subscriptions = profileData.getActiveSubscriptions(userid);
        assertThat(subscriptions.size(), is(2));
    }

}
