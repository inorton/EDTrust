package space.edhits.edtrust;

import space.edhits.edtrust.data.ListSubscription;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by inb on 01/08/2017.
 */
public class ListApiContext {

    public static final int MAX_LISTS_COUNT = 4;
    public static final int MAX_LIST_SIZE = 50;

    final long listId;
    final ListApiContextFactory factory;
    String name;
    boolean isPublic;

    UserApiContext owner;

    public ListApiContext(ListApiContextFactory factory, UserApiContext owner, long listId) {
        this.owner = owner;
        this.name = name;
        this.isPublic = isPublic;
        this.listId = listId;
        this.factory = factory;
    }

    public UserApiContext getViewer() {
        return viewer;
    }

    public void setViewer(UserApiContext viewer) {
        this.viewer = viewer;
    }

    private UserApiContext viewer;

    public UserApiContext getOwner() {
        return owner;
    }

    public String getName() throws UnknownList {
        return factory.getLists().getListName(this.listId);
    }

    public boolean getViewerCanSubscribe() {
        if (viewer != null) {
            ListSubscription listReadAccess = factory.getLists().getListReadAccess(this.listId, viewer.userId);

            if (listReadAccess == ListSubscription.BLOCKED) {
                return false;
            }

            if (listReadAccess == ListSubscription.SUBSCRIBED) {
                return false;
            }

            return true;
        }
        // bug
        throw new RuntimeException("no viewer set");
    }

    public boolean getPublic() {
        return factory.getLists().getListPublic(this.listId);
    }

    public boolean getHidden() {
        return factory.getLists().getListHidden(this.listId);
    }

    private List<UserApiContext> getUserContexts(ArrayList<Long> userIds) throws UnknownUser {
        ArrayList<UserApiContext> usersCtxs = new ArrayList<>();
        for (long userId: userIds) {
            String email = factory.getUsers().getEmail(userId);
            UserApiContext adminUser = owner.getFactory().getUserByEmail(email);
            usersCtxs.add(adminUser);
        }
        return usersCtxs;
    }

    public List<UserApiContext> getAdmins() throws UnknownUser {
        return getUserContexts(factory.getLists().getAdmins(this.listId));
    }

    public List<UserApiContext> getPending() throws UnknownUser {
        return getUserContexts(factory.getLists().getPending(this.listId));
    }

    public List<UserApiContext> getSubscribers() throws UnknownUser {
        return getUserContexts(factory.getLists().getSubscribed(this.listId));
    }

    public List<UserApiContext> getBlocked() throws UnknownUser {
        return getUserContexts(factory.getLists().getBlocked(this.listId));
    }

    public List<String> getItems(int offset, int limit, String state) {
        return factory.getLists().list(this.listId, state, offset, limit);
    }

    public int getSize() {
        return factory.getLists().getSize(this.listId);
    }


    public ListSubscription getSubscriberState(UserApiContext user) {
        return factory.getLists().getListReadAccess(this.listId, user.userId);
    }

    boolean isListAdmin(UserApiContext user) throws UnknownUser {
        if (this.getOwner().userId == user.userId) {
            return true;
        }

        List<UserApiContext> admins = this.getAdmins();
        for (UserApiContext admin : admins) {
            if (admin.userId == user.userId) {
                return true;
            }
        }
        return false;
    }

    boolean canModify(UserApiContext user) throws UnknownUser {
        if (user.admin) {
            return true;
        }
        return isListAdmin(user);
    }

    public void setPublic(UserApiContext user, boolean isPublic) throws UnknownUser {
        if (isPublic != this.getPublic()) {
            if (canModify(user)) {
                factory.getLists().updateListPublic(this.listId, isPublic);
            }
        }
    }

    public void setHidden(UserApiContext user, boolean isHidden) throws UnknownUser {
        if (isHidden != this.getHidden()) {
            if (canModify(user)) {
                factory.getLists().updateListHidden(this.listId, isHidden);
            }
        }
    }

    public void setName(UserApiContext user, String newname) throws NameExists, UnknownUser, UnknownList {
        newname = Sanitizer.listName(newname);
        if (!newname.equals(this.getName())) {
            if (canModify(user)) {
                factory.getLists().updateListName(this.listId, newname);
            }
        }
    }

    public void addCmdr(UserApiContext user, String cmdr, String hostility) throws UnknownUser {
        cmdr = Sanitizer.cmdrName(cmdr);
        if (canModify(user)) {
            if ((this.getSize() < MAX_LIST_SIZE) || isListAdmin(user)) {
                factory.getLists().put(listId, cmdr, hostility);
            }
        }
    }

    public void delCmdr(UserApiContext user, String cmdr) throws UnknownUser {
        cmdr = Sanitizer.cmdrName(cmdr);
        if (canModify(user)) {
            factory.getLists().remove(listId, cmdr);
        }
    }

    public String getHostileState(String cmdr) {
        cmdr = Sanitizer.cmdrName(cmdr);
        return factory.getLists().getHostileState(this.listId, cmdr);
    }

    public String getDescription() throws UnknownList {
        return factory.getLists().getListDescription(this.listId);
    }

}
