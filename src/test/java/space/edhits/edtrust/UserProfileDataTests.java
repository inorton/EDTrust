package space.edhits.edtrust;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import space.edhits.edtrust.data.SQLiteUserProfileData;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;


/**
 * Unit tests for user profile data
 */
@RunWith(JUnit4.class)
public class UserProfileDataTests {

    private static Path dbfile = Paths.get(System.getProperty("user.dir"), "/test-users.sqlite");
    private static String testUsersProfiles = "jdbc:sqlite:" + dbfile.toString();

    @Before
    public void clearDatabase() {
        File file = new File(dbfile.toString());
        if (file.exists()) {
            file.delete();
        }
    }

    @Test
    public void CreateUserProfile() {
        SQLiteUserProfileData profileData = new SQLiteUserProfileData(testUsersProfiles);
        long userid = profileData.makeProfile("test@test.com");
        assertThat(new Long(userid), greaterThan(new Long(0)));
        String apiKey = profileData.getApiKey(userid);
        assertThat(apiKey, notNullValue());
    }
}
