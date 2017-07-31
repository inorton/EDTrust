package space.edhits.edtrust;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;

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
        return this.getUserByEmail(email);
    }

    public UserApiContext getUserByEmail(String email) throws UnknownUser {
        UserApiContext user = new UserApiContext();
        user.load(email, resolver.getUsers(), resolver.getLists());
        return user;
    }
}
