package space.edhits.edtrust;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Get User API Contexts
 */
public class UserApiContextFactory {

    ApiKeyResolver resolver;

    public UserApiContextFactory(ApiKeyResolver resolver) {
        this.resolver = resolver;
    }

    public UserApiContext getUser(String apikey) {
        String cmdr = resolver.getUser(apikey);

        UserApiContext user = new UserApiContext();
        user.load(cmdr);

        return user;
    }
}
