package space.edhits.edtrust;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by inb on 01/08/2017.
 */
public class ListApiContext {

    public static final int MAX_LISTS_COUNT = 4;
    public static final int MAX_LIST_SIZE = 50;

    final long listId;
    final UserApiContext owner;
    String name;
    boolean isPublic;

    public UserApiContext getOwner() {
        return owner;
    }

    public String getName() throws UnknownList {
        return owner.lists.getListName(this.listId);
    }

    public boolean getPublic() {
        return owner.lists.getListPublic(this.listId);
    }

    public boolean getHidden() {
        return owner.lists.getListHidden(this.listId);
    }

    private List<UserApiContext> getUserContexts(ArrayList<Long> userIds) throws UnknownUser {
        ArrayList<UserApiContext> usersCtxs = new ArrayList<>();
        for (long userId: userIds) {
            String email = owner.users.getEmail(userId);
            UserApiContext adminUser = owner.getFactory().getUserByEmail(email);
            usersCtxs.add(adminUser);
        }
        return usersCtxs;
    }

    public List<UserApiContext> getAdmins() throws UnknownUser {
        return getUserContexts(owner.lists.getAdmins(this.listId));
    }

    public List<UserApiContext> getPending() throws UnknownUser {
        return getUserContexts(owner.lists.getPending(this.listId));
    }

    public List<UserApiContext> getSubscribers() throws UnknownUser {
        return getUserContexts(owner.lists.getSubscribed(this.listId));
    }

    public List<UserApiContext> getBlocked() throws UnknownUser {
        return getUserContexts(owner.lists.getBlocked(this.listId));
    }

    public List<String> getItems(int offset, int limit, String state) {
        return owner.lists.list(this.listId, state, offset, limit);
    }

    public int getSize() {
        return owner.lists.getSize(this.listId);
    }

    public ListApiContext(UserApiContext owner, long listId) {
        this.owner = owner;
        this.name = name;
        this.isPublic = isPublic;
        this.listId = listId;
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
                this.owner.lists.updateListPublic(this.listId, isPublic);
            }
        }
    }

    public void setHidden(UserApiContext user, boolean isHidden) throws UnknownUser {
        if (isHidden != this.getHidden()) {
            if (canModify(user)) {
                this.owner.lists.updateListHidden(this.listId, isHidden);
            }
        }
    }

    public void setName(UserApiContext user, String newname) throws NameExists, UnknownUser, UnknownList {
        newname = Sanitizer.listName(newname);
        if (!newname.equals(this.getName())) {
            if (canModify(user)) {
                this.owner.lists.updateListName(this.listId, newname);
            }
        }
    }

    public void addCmdr(UserApiContext user, String cmdr, String hostility) throws UnknownUser {
        cmdr = Sanitizer.cmdrName(cmdr);
        if (canModify(user)) {
            if ((this.getSize() < MAX_LIST_SIZE) || isListAdmin(user)) {
                this.owner.lists.put(listId, cmdr, hostility);
            }
        }
    }

    public void delCmdr(UserApiContext user, String cmdr) throws UnknownUser {
        cmdr = Sanitizer.cmdrName(cmdr);
        if (canModify(user)) {
            this.owner.lists.remove(listId, cmdr);
        }
    }

    public String getHostileState(String cmdr) {
        cmdr = Sanitizer.cmdrName(cmdr);
        return owner.lists.getHostileState(this.listId, cmdr);
    }

    public String getDescription() throws UnknownList {
        return owner.lists.getListDescription(this.listId);
    }
}
