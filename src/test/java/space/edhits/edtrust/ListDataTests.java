package space.edhits.edtrust;


import org.apache.tomcat.util.bcel.Const;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import space.edhits.edtrust.data.CmdrList;
import space.edhits.edtrust.data.SQLiteCmdrList;

import java.io.IOException;
import java.util.ArrayList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.core.Is.is;


/**
 * Unit tests for user profile data
 */
@RunWith(JUnit4.class)
public class ListDataTests extends TestCommon {

    @Rule
    public ExpectedException expected = ExpectedException.none();

    protected CmdrList listData;

    @Before
    public void setupProfileData() {
        listData = new SQLiteCmdrList(testDb);
    }

    @After
    public void closeDatabase() throws IOException {
        listData.close();
    }

    @Test
    public void testEmptyCreate() throws Exception {
        long owernid = 1;
        long list = listData.createList(owernid, "stuff");
        assertThat(list, greaterThan(new Long(0)));
        String name = listData.getListName(list);
        assertThat(name, is(equalTo("stuff")));

        ArrayList<String> lists = listData.lists(owernid);
        assertThat(lists.size(), is(equalTo(1)));
    }

    @Test
    public void testNoDuplicateNames() throws Exception {
        long owernid = 1;
        listData.createList(owernid, "stuff");
        expected.expect(NameExists.class);
        listData.createList(owernid, "stuff");
    }

    @Test
    public void testUpdateNoDuplicateNames() throws Exception {
        long owernid = 1;
        listData.createList(owernid, "stuff");
        long stuff2 = listData.createList(owernid, "stuff2");

        expected.expect(NameExists.class);
        listData.updateListName(stuff2, "stuff");
    }

    @Test
    public void testDelete() throws NameExists {
        long owernid = 1;
        long stuff = listData.createList(owernid, "stuff");
        listData.deleteList(stuff);
        listData.createList(owernid, "stuff");
    }

    @Test
    public void testSetDescription() throws Exception {
        long owernid = 1;
        long stuff = listData.createList(owernid, "stuff");
        String desc = TestHelpers.randomString();
        listData.updateListDescription(stuff, desc);
        assertThat(listData.getListDescription(stuff), is(equalTo(desc)));
    }

    @Test
    public void testSetAdmin() throws NameExists {
        long owernid = 1;
        long adminid = 2;
        long stuff = listData.createList(owernid, "stuff");
        assertThat(listData.getAdmin(stuff, adminid), is(false));

        listData.setAdmin(stuff, adminid, true);
        assertThat(listData.getAdmin(stuff, adminid), is(true));

        listData.setAdmin(stuff, adminid, false);
        assertThat(listData.getAdmin(stuff, adminid), is(false));
    }

    @Test
    public void testSetPublic() throws NameExists {
        long owernid = 1;
        long stuff = listData.createList(owernid, "stuff");
        assertThat(listData.getListPublic(stuff), is(false));

        listData.updateListPublic(stuff, true);
        assertThat(listData.getListPublic(stuff), is(true));

        listData.updateListPublic(stuff, false);
        assertThat(listData.getListPublic(stuff), is(false));
    }

    @Test
    public void testAddRemoveNames() throws NameExists {
        long owernid = 1;
        long stuff = listData.createList(owernid, "stuff");

        listData.put(stuff, "fred", Constants.RESPONSE_STATUS_HOSTILE);
        listData.put(stuff, "george", Constants.RESPONSE_STATUS_HOSTILE);
        listData.put(stuff, "sandy", Constants.RESPONSE_STATUS_HOSTILE);
        listData.put(stuff, "dave", Constants.RESPONSE_STATUS_FRIENDLY);

        ArrayList<String> bad = listData.list(stuff, Constants.RESPONSE_STATUS_HOSTILE, 0, 10);
        assertThat(bad.size(), is(3));

        ArrayList<String> good = listData.list(stuff, Constants.RESPONSE_STATUS_FRIENDLY, 0, 10);
        assertThat(good.size(), is(1));

        // now delete one
        listData.remove(stuff, "fred");
        ArrayList<String> smallbad = listData.list(stuff, Constants.RESPONSE_STATUS_HOSTILE, 0, 10);
        assertThat(smallbad.size(), is(2));
    }

    @Test
    public void testGetHostile() throws NameExists {
        long owernid = 1;
        long stuff = listData.createList(owernid, "stuff");

        listData.put(stuff, "fred", Constants.RESPONSE_STATUS_HOSTILE);
        listData.put(stuff, "dave", Constants.RESPONSE_STATUS_FRIENDLY);

        String fred = listData.getHostileState(stuff, "fred");
        String dave = listData.getHostileState(stuff, "dave");

        assertThat(fred, equalTo(Constants.RESPONSE_STATUS_HOSTILE));
        assertThat(dave, equalTo(Constants.RESPONSE_STATUS_FRIENDLY));
    }

    @Test
    public void testPublicLists() throws NameExists {
        long owernid = 1;
        long stuff = listData.createList(owernid, "stuff");
        ArrayList<String> publicLists = listData.publicLists();
        assertThat(publicLists.size(), is(0));
        listData.updateListPublic(stuff, true);

        ArrayList<String> publicLists2 = listData.publicLists();
        assertThat(publicLists2.size(), is(1));
    }

    @Test
    public void testGetListId() throws NameExists, UnknownList {
        long owernid = 1;
        long stuff = listData.createList(owernid, "stuff");
        long found = listData.getList("stuff");
        assertThat(found, is(stuff));

        expected.expect(UnknownList.class);
        listData.getList("nolist");
    }
}
