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

    public UserApiContext getUser(String apikey) throws UnknownUser {
        String email = resolver.getUser(apikey);

        UserApiContext user = new UserApiContext();
        user.load(email, resolver.getUsers(), resolver.getLists());

        return user;
    }
}
