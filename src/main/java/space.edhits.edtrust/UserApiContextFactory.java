package space.edhits.edtrust;

/**
 * Get User API Contexts
 */
public class UserApiContextFactory {

    final ApiKeyResolver resolver;

    public UserApiContextFactory(ApiKeyResolver resolver) {
        this.resolver = resolver;
    }

    public UserApiContext getUser(String apikey) throws UnknownUser {
        String email = resolver.getUser(apikey);
        return this.getUserByEmail(email);
    }

    public UserApiContext getUserByEmail(String email) throws UnknownUser {
        UserApiContext user = new UserApiContext(this);
        user.load(email, resolver.getUsers(), resolver.getLists());
        return user;
    }
}
