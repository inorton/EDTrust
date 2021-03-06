package space.edhits.edtrust;

import space.edhits.edtrust.data.CmdrList;
import space.edhits.edtrust.data.UserProfileData;


public class ApiKeyResolver {

    final UserProfileData users;
    final CmdrList lists;

    public ApiKeyResolver(UserProfileData users, CmdrList lists)
    {
        this.users = users;
        this.lists = lists;
    }

    /**
     * If given an API Key, return the user's email address
     */
    public String getUser(String apikey) {
        try {
            return getUserById(users.getIdFromKey(apikey));
        } catch (UnknownUser unknownUser) {
            throw new UnauthorizedApiKey();
        }
    }

    public String getUserById(long userId) {
        try {
            return users.getEmail(userId);
        } catch (UnknownUser unknownUser) {
            throw new UnauthorizedApiKey();
        }
    }

    public CmdrList getLists(){
        return this.lists;
    }
    public UserProfileData getUsers() {
        return this.users;
    }
}
