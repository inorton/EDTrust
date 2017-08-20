package space.edhits.edtrust;

import space.edhits.edtrust.data.CmdrList;
import space.edhits.edtrust.data.UserProfileData;

import java.util.ArrayList;

public class ListApiContextFactory {

    CmdrList lists;
    UserProfileData users;
    UserApiContextFactory userFactory;

    public ListApiContextFactory(UserApiContextFactory userFactory, CmdrList listData, UserProfileData userProfileData){
        this.lists = listData;
        this.users = userProfileData;
        this.userFactory = userFactory;
    }

    public CmdrList getLists() {
        return lists;
    }

    public UserProfileData getUsers() {
        return users;
    }

    public ListApiContext getList(long listId) throws UnknownList, UnknownUser {
        long ownerId = lists.getOwner(listId);
        UserApiContext owner = userFactory.getUserById(ownerId);
        return new ListApiContext(this, owner, listId);
    }

    public ListApiContext getList(String name) throws UnknownList, UnknownUser {
        long listId = lists.getList(name);
        return getList(listId);
    }

    public ArrayList<ListApiContext> getLists(ArrayList<Long> listIds) throws UnknownList, UnknownUser {

        ArrayList<ListApiContext> listCtxts = new ArrayList<>();

        for (long listId : listIds) {
            UserApiContext owner = userFactory.getUserById(lists.getOwner(listId));
            listCtxts.add(getList(listId));
        }

        return listCtxts;
    }
}
