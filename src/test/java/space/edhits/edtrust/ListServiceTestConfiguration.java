package space.edhits.edtrust;

import org.apache.tomcat.util.bcel.Const;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.*;
import space.edhits.edtrust.data.CmdrList;
import space.edhits.edtrust.data.SQLiteCmdrList;
import space.edhits.edtrust.data.SQLiteUserProfileData;
import space.edhits.edtrust.data.UserProfileData;

import java.lang.annotation.RetentionPolicy;
import java.util.HashMap;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.isNotNull;
import static org.mockito.Mockito.when;

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
