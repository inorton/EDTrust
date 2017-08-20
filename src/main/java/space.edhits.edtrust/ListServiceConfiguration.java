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

    private static final Path dbfile = Paths.get(System.getProperty("user.dir"), "/edtrust.sqlite");
    protected static final String edtrustDb = "jdbc:sqlite:" + dbfile.toString();


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
    public CmdrList cmdrList(UserProfileData profiles) {
        SQLiteCmdrList cmdrLists = new SQLiteCmdrList(edtrustDb);
        profiles.init(cmdrLists);
        return cmdrLists;
    }

    @Bean
    public ListApiContextFactory listApiContextFactory(
            UserApiContextFactory userFactory,
            CmdrList listData,
            UserProfileData userData) {
        ListApiContextFactory factory = new ListApiContextFactory(userFactory, listData, userData);
        userFactory.setListFactory(factory);
        return factory;
    }
}
