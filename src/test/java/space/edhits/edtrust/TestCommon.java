package space.edhits.edtrust;

import org.junit.After;
import org.junit.Before;
import space.edhits.edtrust.data.SQLiteUserProfileData;
import space.edhits.edtrust.data.UserProfileData;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import static java.lang.Thread.sleep;

/**
 * Created by inb on 30/07/2017.
 */
public class TestCommon {
    private static Path dbfile = Paths.get(System.getProperty("user.dir"), "/test.sqlite");
    public static String testDb = "jdbc:sqlite:" + dbfile.toString();

    @Before
    public void clearDatabase() throws InterruptedException {
        clearDatabaseFile();
    }

    public static void clearDatabaseFile() throws InterruptedException {
        File file = dbfile.toFile();
        if (file.exists()) {
            int counter = 1000;
            while (!file.delete()) {
                sleep(TimeUnit.MILLISECONDS.toMillis(10));
                if (counter-- == 0) {
                    throw new RuntimeException("could not delete database file!");
                }
            }
        }
    }


    protected long makeUser(String email, UserProfileData users) {
        return users.makeProfile(email);
    }
}
