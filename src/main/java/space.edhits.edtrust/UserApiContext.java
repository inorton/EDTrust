package space.edhits.edtrust;

import space.edhits.edtrust.data.CmdrList;
import space.edhits.edtrust.data.UserProfileData;


public class UserApiContext {

    UserProfileData users = null;
    CmdrList lists = null;
    long userId = 0;
    boolean admin = false;

    public void load(String email, UserProfileData userProfiles, CmdrList lists) throws UnknownUser {
        this.users = userProfiles;
        this.lists = lists;
        userId = users.getId(email);
        admin = users.getAdminStatus(userId);
    }

    public ContactResponse check(String cmdr) {
        ContactResponse contactResponse = new ContactResponse();
        contactResponse.setCmdr(cmdr);
        contactResponse.setStatus(Constants.RESPONSE_STATUS_UNKNOWN);

        return contactResponse;
    }
}
