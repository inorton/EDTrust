package space.edhits.edtrust;

import space.edhits.edtrust.data.CmdrList;
import space.edhits.edtrust.data.UserProfileData;

import java.util.ArrayList;


public class UserApiContext {
    public UserApiContextFactory getFactory() {
        return factory;
    }

    final UserApiContextFactory factory;
    final CmdrList lists;
    private UserProfileData users = null;

    long userId = 0;
    boolean admin = false;
    boolean currentListAdmin = false;

    public long getUserId() {
        return userId;
    }

    public boolean isCurrentListAdmin() {
        return currentListAdmin;
    }

    public boolean getAdmin() {
        return admin;
    }

    public UserProfileData getUsers(){
        return users;
    }

    public String getEmail() throws UnknownUser {
        return users.getEmail(this.userId);
    }

    public String getApiKey() {
        return this.users.getApiKey(this.userId);
    }

    public UserApiContext(UserApiContextFactory factory) {
        this.lists = factory.getResolver().getLists();
        this.factory = factory;
    }

    public void load(String email, UserProfileData userProfiles, CmdrList lists) throws UnknownUser {
        this.users = userProfiles;
        userId = users.getId(email);
        admin = users.getAdminStatus(userId);
    }

    public ArrayList<ListApiContext> getOwnedLists() throws UnknownList, UnknownUser {
        ArrayList<ListApiContext> owned = new ArrayList<>();
        for (String listName :  lists.lists(this.userId)) {
            owned.add(factory.getListFactory().getList(listName));
        }

        return owned;
    }

    public ContactResponse check(String cmdr) throws UnknownList {
        ContactResponse contactResponse = new ContactResponse();
        contactResponse.setCmdr(cmdr);
        contactResponse.setStatus(Constants.RESPONSE_STATUS_UNKNOWN);

        // check our own lists
        ArrayList<String> owned = this.lists.lists(userId);
        boolean found = false;
        for (String name: owned) {
            long listId = this.lists.getList(name);
            String state = this.lists.getHostileState(listId, cmdr);
            if (!state.equals(Constants.RESPONSE_STATUS_UNKNOWN)) {
                contactResponse.setStatus(state);
                found = true;
                break;
            }
        }

        // if not found, check our subscribed lists
        if (!found) {
            ArrayList<Long> subscriptions = this.users.getActiveSubscriptions(userId);
            for (Long listId: subscriptions) {
                // is this public
                boolean isAdmin = this.lists.getAdmin(listId, userId);
                // or are we an admin for it?
                boolean isPublic = this.lists.getListPublic(listId);
                // or are we granted r/o access?
                ArrayList<Long> readers = this.lists.getSubscribed(listId);
                if (isAdmin || isPublic || readers.contains(listId)) {
                    String state = this.lists.getHostileState(listId, cmdr);
                    if (!state.equals(Constants.RESPONSE_STATUS_UNKNOWN)) {
                        contactResponse.setStatus(state);
                        break;
                    }
                }
            }
        }

        return contactResponse;
    }
}
