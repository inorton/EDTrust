package space.edhits.edtrust;

/**
 * Get User API Contexts
 */
public class UserApiContextFactory {

    private ListApiContextFactory listFactory;

    public ListApiContextFactory getListFactory() {
        return listFactory;
    }

    public void setListFactory(ListApiContextFactory listFactory) {
        this.listFactory = listFactory;
    }

    public ApiKeyResolver getResolver() {
        return resolver;
    }

    final ApiKeyResolver resolver;

    public UserApiContextFactory(ApiKeyResolver resolver) {
        this.resolver = resolver;
    }

    public UserApiContext getUserById(long userId) throws UnknownUser {
        return this.getUserByEmail(resolver.getUserById(userId));
    }

    public UserApiContext getUser(String apikey) throws UnknownUser {
        return this.getUserByEmail(resolver.getUser(apikey));
    }

    public UserApiContext getUserByEmail(String email) throws UnknownUser {
        UserApiContext user = new UserApiContext(this);
        user.load(email, resolver.getUsers(), resolver.getLists());
        return user;
    }
}
