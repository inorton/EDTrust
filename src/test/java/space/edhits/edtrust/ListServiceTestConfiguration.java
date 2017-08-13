package space.edhits.edtrust;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import space.edhits.edtrust.data.CmdrList;
import space.edhits.edtrust.data.SQLiteCmdrList;
import space.edhits.edtrust.data.SQLiteUserProfileData;
import space.edhits.edtrust.data.UserProfileData;

/**
 * Beans for tests
 */
@Configuration
public class ListServiceTestConfiguration {

    @Bean
    @Primary
    public UserProfileData testUserProfileData() throws InterruptedException {
        TestCommon.clearDatabaseFile();
        SQLiteUserProfileData users = new SQLiteUserProfileData(TestCommon.testDb);
        // add some users
        users.makeProfile(TestHelpers.randomEmail());

        // add the main test user
        users.makeProfile(TestHelpers.TEST_USER_EMAIL);

        return users;
    }

    @Bean
    @Primary
    public CmdrList testCmdrList(UserProfileData users) throws Exception {
        SQLiteCmdrList lists = new SQLiteCmdrList(TestCommon.testDb);

        // make a small list for the test user
        long user = users.getId(TestHelpers.TEST_USER_EMAIL);
        long baddies = lists.createList(user, TestHelpers.TEST_USER_LIST);
        lists.put(baddies, TestHelpers.randomString(), Constants.RESPONSE_STATUS_HOSTILE);
        lists.put(baddies, TestHelpers.randomString(), Constants.RESPONSE_STATUS_HOSTILE);
        lists.put(baddies, TestHelpers.TEST_BAD_CMDR, Constants.RESPONSE_STATUS_HOSTILE);

        return lists;
    }

}
