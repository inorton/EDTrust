package space.edhits.edtrust.data;

import space.edhits.edtrust.NameExists;
import space.edhits.edtrust.UnknownList;

import java.io.Closeable;
import java.util.ArrayList;

/**
 * Query or update a list
 */
public interface CmdrList extends Closeable {
    ArrayList<String> lists(long ownerId);
    ArrayList<String> publicLists();
    long getList(String name) throws UnknownList;
    int getSize(long listId);
    ArrayList<String> list(long listId, String hostileState, int offset, int limit);
    void put(long listId, String cmdr, String hostileState);
    void remove(long listId, String cmdr);
    String getHostileState(long listId, String cmdr);
    long getOwner(long listId) throws UnknownList;
    void setAdmin(long listId, long userId, boolean isAdmin);
    boolean getAdmin(long listId, long userId);
    ArrayList<Long> getAdmins(long listId);

    ArrayList<Long> getSubscribed(long listId);
    ArrayList<Long> getBlocked(long listId);
    ArrayList<Long> getPending(long listId);
    void updateListAccess(long listId, long userId, ListSubscription access);

    long createList(long owner, String name) throws NameExists;
    void updateListName(long listId, String name) throws NameExists;
    void updateListDescription(long listId, String desc);
    void deleteList(long listId);

    String getListName(long listId) throws UnknownList;
    String getListDescription(long listId) throws UnknownList;

    boolean getListPublic(long listId);
    void updateListPublic(long listId, boolean isPublic);

    boolean getListHidden(long listId);
    void updateListHidden(long listId, boolean isPublic);
}
