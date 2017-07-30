package space.edhits.edtrust;

/**
 * If given an API Key, return a username (cmdr)
 */
public class ApiKeyResolver {

    public String getUser(String apikey) {
        throw new UnauthorizedApiKey();
    }
}
