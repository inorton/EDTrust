package space.edhits.edtrust.data;

import java.util.ArrayList;

/**
 * Query or update a list
 */
public interface CmdrList {

    ArrayList<String> list(long listId, String hostileState, int offset, int limit);
    void add(long listId, String hostileState, String cmdr);
    void remove(long listId, String hostileState, String cmdr);
    String getHostileState(long listId, String cmdr);
}
