package space.edhits.edtrust;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by inb on 01/08/2017.
 */
public class ListApiContext {

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

    public boolean isPublic() {
        return owner.lists.getListPublic(this.listId);
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

    public List<String> getItems(int offset, int limit, String state) {
        return owner.lists.list(this.listId, state, offset, limit);
    }

    public int size() {
        return owner.lists.getSize(this.listId);
    }

    public ListApiContext(UserApiContext owner, long listId) {
        this.owner = owner;
        this.name = name;
        this.isPublic = isPublic;
        this.listId = listId;
    }
}
