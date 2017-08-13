package space.edhits.edtrust;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by inb on 01/08/2017.
 */
public class ListApiContext {

    public static final int MAX_LISTS_COUNT = 4;
    public static final int MAX_LIST_SIZE = 50;

    long listId;
    UserApiContext owner;
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

    public List<UserApiContext> getAdmins() throws UnknownUser {
        ArrayList<UserApiContext> admins = new ArrayList<>();
        ArrayList<Long> adminIds = owner.lists.getAdmins(this.listId);
        for (long userId: adminIds) {
            String email = owner.users.getEmail(userId);
            UserApiContext adminUser = owner.getFactory().getUserByEmail(email);
            admins.add(adminUser);
        }
        return admins;
    }

    public List<UserApiContext> getPending() throws UnknownUser {
        return null;
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
}
