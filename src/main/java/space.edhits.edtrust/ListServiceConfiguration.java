package space.edhits.edtrust;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import space.edhits.edtrust.data.CmdrList;
import space.edhits.edtrust.data.SQLiteCmdrList;
import space.edhits.edtrust.data.SQLiteUserProfileData;
import space.edhits.edtrust.data.UserProfileData;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Beans!
 */
@Configuration
public class ListServiceConfiguration {

    private static Path dbfile = Paths.get(System.getProperty("user.dir"), "/edtrust.sqlite");
    protected static String edtrustDb = "jdbc:sqlite:" + dbfile.toString();


    @Bean
    public UserApiContextFactory apiContextFactory(ApiKeyResolver resolver) {
        return new UserApiContextFactory(resolver);
    }

    @Bean
    public ApiKeyResolver apiKeyResolver(UserProfileData users, CmdrList lists) {
        return new ApiKeyResolver(users, lists);
    }

    @Bean
    public UserProfileData userProfileData() {
        return new SQLiteUserProfileData(edtrustDb);
    }

    @Bean
    public CmdrList cmdrList() {
        return new SQLiteCmdrList(edtrustDb);
    }
}
