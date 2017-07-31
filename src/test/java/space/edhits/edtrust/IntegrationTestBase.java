package space.edhits.edtrust;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.springframework.beans.factory.annotation.Autowired;
import space.edhits.edtrust.data.CmdrList;
import space.edhits.edtrust.data.UserProfileData;

import java.io.IOException;

public abstract class IntegrationTestBase {

    @Rule
    public ExpectedException expected = ExpectedException.none();

    @Autowired
    protected ListServiceController listService;

    @Autowired
    protected UserProfileData users;

    @Autowired
    protected CmdrList lists;

    static CmdrList staticCmdrLists;
    static UserProfileData staticUsers;
    @Before
    public void memo(){
        staticCmdrLists = lists;
        staticUsers = users;
    }

    @AfterClass
    public synchronized static void finished() throws IOException {
        if (staticUsers != null) {
            staticUsers.close();
            staticUsers = null;
        }

        if (staticCmdrLists != null) {
            staticCmdrLists.close();
            staticCmdrLists = null;
        }
    }
}
