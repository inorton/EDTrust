package space.edhits.edtrust.data;

import org.sqlite.SQLiteErrorCode;
import org.sqlite.SQLiteException;
import space.edhits.edtrust.Constants;
import space.edhits.edtrust.NameExists;
import space.edhits.edtrust.UnknownList;

import java.sql.*;
import java.util.ArrayList;

/**
 * Created by inb on 30/07/2017.
 */
public class SQLiteCmdrList extends SQLiteDataSource implements CmdrList {

    public SQLiteCmdrList(String url) {
        super(url);
    }

    private static String ADMIN_TABLE = "cmdrListAdmins";
    private static String SUBSCRIBER_TABLE = "cmdrListReaders";
    private static String BLOCKED_TABLE = "cmdrListBlocked";
    private static String PENDING_TABLE = "cmdrListPending";

    private static String[] BOOLEAN_TABLES = new String[] {
            ADMIN_TABLE,
            SUBSCRIBER_TABLE,
            BLOCKED_TABLE,
            PENDING_TABLE
    };

    @Override
    protected void makeTables() throws SQLException {
        try (Statement sth = connection.createStatement()) {
            String table = new StringBuilder()
                    .append("CREATE TABLE IF NOT EXISTS cmdrListInfo (")
                    .append(" id INTEGER PRIMARY KEY, ")
                    .append(" name TEXT UNIQUE, ")
                    .append(" description TEXT, ")
                    .append(" owner INTEGER, ")
                    .append(" hidden INTEGER, ")
                    .append(" public INTEGER)").toString();
            sth.execute(table);
        }

        try (Statement sth = connection.createStatement()) {
            String table = new StringBuilder()
                    .append("CREATE TABLE IF NOT EXISTS cmdrs (")
                    .append(" id INTEGER PRIMARY KEY, ")
                    .append(" cmdr TEXT UNIQUE) ").toString();
            sth.execute(table);
        }

        try (Statement sth = connection.createStatement()) {
            String table = new StringBuilder()
                    .append("CREATE TABLE IF NOT EXISTS cmdrLists (")
                    .append(" list INTEGER, ")
                    .append(" cmdr INTEGER, ")
                    .append(" hostility TEXT)").toString();
            sth.execute(table);
        }
        try (Statement sth = connection.createStatement()) {
            String index = new StringBuilder()
                    .append("CREATE INDEX IF NOT EXISTS I_cmdrLists_hostility")
                    .append(" ON cmdrLists ")
                    .append(" (hostility) ").toString();
            sth.execute(index);
        }

        try (Statement sth = connection.createStatement()) {
            String index = new StringBuilder()
                    .append("CREATE UNIQUE INDEX IF NOT EXISTS I_cmdrLists")
                    .append(" ON cmdrLists ")
                    .append(" (list, cmdr) ").toString();
            sth.execute(index);
        }

        /* common boolean access tables */
    for (String tableName: BOOLEAN_TABLES) {
            try (Statement sth = connection.createStatement()) {
                String table = new StringBuilder()
                        .append("CREATE TABLE IF NOT EXISTS ")
                        .append(tableName)
                        .append(" (")
                        .append(" list INTEGER, ")
                        .append(" user INTEGER) ").toString();
                sth.execute(table);
            }

            try (Statement sth = connection.createStatement()) {
                String index = new StringBuilder()
                        .append("CREATE UNIQUE INDEX IF NOT EXISTS I_")
                        .append(tableName)
                        .append(" ON ")
                        .append(tableName)
                        .append(" (list, user) ").toString();
                sth.execute(index);
            }
        }

    }

    @Override
    public int getSize(long listId) {
        try {
            ArrayList<String> cmdrs = new ArrayList<>();
            try (PreparedStatement sth = connection.prepareStatement(
                    "SELECT count(*) FROM cmdrLists " +
                            " WHERE cmdrLists.list == ? ")) {
                sth.setLong(1, listId);
                ResultSet resultSet = sth.executeQuery();

                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
            }
            return 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ArrayList<String> list(long listId, String hostileState, int offset, int limit) {
        // select cmdrs.cmdr from cmdrLists INNER JOIN cmdrs
        //   ON cmdrLists.cmdr = cmdrs.id WHERE hostility = "hostile";
        try {
            ArrayList<String> cmdrs = new ArrayList<>();
            try (PreparedStatement sth = connection.prepareStatement(
                    "SELECT cmdrs.cmdr FROM cmdrLists INNER JOIN" +
                            " cmdrs ON cmdrLists.cmdr = cmdrs.id " +
                            " WHERE cmdrLists.list == ? " +
                            " AND cmdrLists.hostility == ? " +
                            " ORDER BY cmdrs.cmdr")) {
                sth.setLong(1, listId);
                sth.setString(2, hostileState);
                ResultSet resultSet = sth.executeQuery();

                while (resultSet.next()) {
                    cmdrs.add(resultSet.getString(1));
                }
            }
            return cmdrs;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void put(long listId, String cmdr, String hostility) {
        long cmdrId = getCmdrId(cmdr);
        try (Connection writer = getWriteConnection()) {
            PreparedStatement sth = writer.prepareStatement(
                    "REPLACE INTO cmdrLists (list, cmdr, hostility)" +
                            " VALUES (?, ?, ?)");
            sth.setLong(1, listId);
            sth.setLong(2, cmdrId);
            sth.setString(3, hostility);
            sth.execute();
            writer.commit();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void remove(long listId, String cmdr) {
        long cmdrId = getCmdrId(cmdr);
        try (Connection writer = getWriteConnection()) {
            PreparedStatement sth = writer.prepareStatement(
                    "DELETE FROM cmdrLists WHERE list == ? AND cmdr == ?");
            sth.setLong(1, listId);
            sth.setLong(2, cmdrId);
            sth.execute();
            writer.commit();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private long addCmdr(String cmdr) throws SQLException {
        try (Connection writer = getWriteConnection()){
            try (PreparedStatement sth = writer.prepareStatement(
                    "REPLACE INTO cmdrs (cmdr) VALUES(?)", Statement.RETURN_GENERATED_KEYS)) {
                sth.setString(1, cmdr);
                sth.execute();
                ResultSet generatedKeys = sth.getGeneratedKeys();

                writer.commit();
                return generatedKeys.getLong(1);
            }
        }
    }

    private long getCmdrId(String cmdr) {
        try {
            try (PreparedStatement sth = connection.prepareStatement(
                    "SELECT id FROM cmdrs " +
                            "WHERE cmdr == ?")) {
                sth.setString(1, cmdr);
                ResultSet resultSet = sth.executeQuery();

                if (resultSet.next()) {
                    return resultSet.getLong(1);
                }
            }
            return addCmdr(cmdr);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getHostileState(long listId, String cmdr) {
        long cmdrId = getCmdrId(cmdr);

        try (PreparedStatement sth = connection.prepareStatement(
                "SELECT hostility FROM cmdrLists " +
                    "WHERE list == ? AND cmdr == ?")) {
            sth.setLong(1, listId);
            sth.setLong(2, cmdrId);

            ResultSet resultSet = sth.executeQuery();
            if (!resultSet.next()) {
                return Constants.RESPONSE_STATUS_UNKNOWN;
            }
            return resultSet.getString("hostility");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void setBooleanAccess(String tableName, long listId, long userId, boolean insert) {
        try (Connection writer = getWriteConnection()) {

            PreparedStatement sth;
            if (insert) {
                sth = writer.prepareStatement(new StringBuilder().append("REPLACE INTO ").append(tableName).append(" (list, user) ")
                        .append(" VALUES( ?, ? )").toString());
            } else {
                sth = writer.prepareStatement(
                        "DELETE FROM " + tableName +
                                " WHERE list == ? AND user == ?");
            }
            sth.setLong(1, listId);
            sth.setLong(2, userId);
            sth.execute();
            writer.commit();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean getBooleanAccess(String tableName, long listId, long userId) {
        try {
            try (PreparedStatement sth = connection.prepareStatement(
                    "SELECT count(user) FROM " + tableName +
                            " WHERE list == ? AND user == ?")) {
                sth.setLong(1, listId);
                sth.setLong(2, userId);
                ResultSet resultSet = sth.executeQuery();
                if (resultSet.next()) {
                    return (resultSet.getLong(1) == 1);
                }
            }
            return false;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private ArrayList<Long> getBooleanAccessMembers(String tableName, long listId) {
        ArrayList<Long> readers = new ArrayList<>();
        try {
            try (PreparedStatement sth = connection.prepareStatement(
                    "SELECT user FROM " + tableName +
                            " WHERE list == ? ")) {
                sth.setLong(1, listId);
                ResultSet resultSet = sth.executeQuery();

                while (resultSet.next()) {
                    readers.add (resultSet.getLong(1));
                }
            }
            return readers;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void setAdmin(long listId, long userId, boolean isAdmin) {
        setBooleanAccess(ADMIN_TABLE, listId, userId, isAdmin);
    }

    @Override
    public boolean getAdmin(long listId, long userId) {
        return getBooleanAccess(ADMIN_TABLE, listId, userId);
    }


    @Override
    public ArrayList<Long> getAdmins(long listId) {
        ArrayList<Long> admins = new ArrayList<>();
        try {
            try (PreparedStatement sth = connection.prepareStatement(
                    "SELECT user FROM cmdrListAdmins " +
                            " WHERE list == ? ")) {
                sth.setLong(1, listId);
                ResultSet resultSet = sth.executeQuery();

                while (resultSet.next()) {
                    admins.add (resultSet.getLong(1));
                }
            }
            return admins;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public long createList(long owner, String name) throws NameExists {
        try {
            try (Connection writer = getWriteConnection()) {
                String sql = new StringBuilder()
                        .append("INSERT INTO cmdrListInfo ")
                        .append(" (owner, public, name) ")
                        .append(" VALUES( ?, ?, ?)").toString();
                PreparedStatement sth = writer.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                sth.setLong(1, owner);
                sth.setBoolean(2, false);
                sth.setString(3, name);
                sth.execute();
                ResultSet generatedKeys = sth.getGeneratedKeys();
                writer.commit();
                if (generatedKeys.next()){
                    return generatedKeys.getLong(1);
                } else {
                    throw new RuntimeException("could not create list");
                }
            }
        } catch (SQLException e) {
            if (e instanceof SQLiteException) {
                if (((SQLiteException)e).getResultCode() == SQLiteErrorCode.SQLITE_CONSTRAINT_UNIQUE) {
                    throw new NameExists(name);
                }
            }
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteList(long listId) {
        try (Connection writer = getWriteConnection()) {

            PreparedStatement sth;
            sth = writer.prepareStatement(new StringBuilder()
                    .append("DELETE FROM cmdrListInfo ")
                    .append(" WHERE id == ?").toString());
            sth.setLong(1, listId);
            sth.execute();


            sth = writer.prepareStatement(new StringBuilder()
                    .append("DELETE FROM cmdrLists ")
                    .append(" WHERE list == ?").toString());
            sth.setLong(1, listId);
            sth.execute();

            for (String tableName : BOOLEAN_TABLES) {
                sth = writer.prepareStatement(new StringBuilder().append("DELETE FROM ").append(tableName)
                        .append(" WHERE list == ?").toString());
                sth.setLong(1, listId);
                sth.execute();
            }

            writer.commit();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateListName(long listId, String name) throws NameExists {
        try (Connection writer = getWriteConnection()) {

            PreparedStatement sth;
            sth = writer.prepareStatement(new StringBuilder()
                    .append("UPDATE cmdrListInfo ")
                    .append(" SET name = ? ")
                    .append(" WHERE id == ?").toString());
            sth.setLong(2, listId);
            sth.setString(1, name);
            sth.execute();
            writer.commit();
        } catch (SQLException e) {
            if (e instanceof SQLiteException) {
                if (((SQLiteException)e).getResultCode() == SQLiteErrorCode.SQLITE_CONSTRAINT_UNIQUE) {
                    throw new NameExists(name);
                }
            }
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateListDescription(long listId, String desc) {
        try (Connection writer = getWriteConnection()) {

            PreparedStatement sth;
            sth = writer.prepareStatement(new StringBuilder()
                    .append("UPDATE cmdrListInfo ")
                    .append(" SET description = ? ")
                    .append(" WHERE id == ?").toString());
            sth.setLong(2, listId);
            sth.setString(1, desc);
            sth.execute();
            writer.commit();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ArrayList<String> lists(long ownerId) {
        ArrayList<String> lists = new ArrayList<>();
        try {
            try (PreparedStatement sth = connection.prepareStatement(
                    "SELECT name FROM cmdrListInfo " +
                            " WHERE owner == ? ORDER BY name")) {
                sth.setLong(1, ownerId);
                ResultSet resultSet = sth.executeQuery();

                while (resultSet.next()) {
                    lists.add(resultSet.getString(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return lists;
    }

    @Override
    public ArrayList<String> publicLists() {
        ArrayList<String> lists = new ArrayList<>();
        try {
            try (PreparedStatement sth = connection.prepareStatement(
                    "SELECT name FROM cmdrListInfo " +
                            " WHERE public == ? ORDER BY name")) {
                sth.setBoolean(1, true);
                ResultSet resultSet = sth.executeQuery();

                while (resultSet.next()) {
                    lists.add(resultSet.getString(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return lists;
    }


    @Override
    public long getOwner(long listId) throws UnknownList {
        try {
            try (PreparedStatement sth = connection.prepareStatement(
                    "SELECT owner FROM cmdrListInfo " +
                            " WHERE id == ? ")) {
                sth.setLong(1, listId);
                ResultSet resultSet = sth.executeQuery();

                if (resultSet.next()) {
                    return (resultSet.getLong(1));
                }
            }
            throw new UnknownList();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getListName(long listId) throws UnknownList {
        try {
            try (PreparedStatement sth = connection.prepareStatement(
                    "SELECT name FROM cmdrListInfo " +
                            " WHERE id == ? ")) {
                sth.setLong(1, listId);
                ResultSet resultSet = sth.executeQuery();

                if (resultSet.next()) {
                    return (resultSet.getString(1));
                }
            }
            throw new UnknownList();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getListDescription(long listId) throws UnknownList {
        try {
            try (PreparedStatement sth = connection.prepareStatement(
                    "SELECT description FROM cmdrListInfo " +
                            " WHERE id == ? ")) {
                sth.setLong(1, listId);
                ResultSet resultSet = sth.executeQuery();

                if (resultSet.next()) {
                    return (resultSet.getString(1));
                }
            }
            throw new UnknownList();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean getListHidden(long listId) {
        try {
            try (PreparedStatement sth = connection.prepareStatement(
                    "SELECT hidden FROM cmdrListInfo " +
                            " WHERE id == ? ")) {
                sth.setLong(1, listId);
                ResultSet resultSet = sth.executeQuery();

                if (resultSet.next()) {
                    return (resultSet.getBoolean(1));
                }
            }
            return false;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean getListPublic(long listId) {
        try {
            try (PreparedStatement sth = connection.prepareStatement(
                    "SELECT public FROM cmdrListInfo " +
                            " WHERE id == ? ")) {
                sth.setLong(1, listId);
                ResultSet resultSet = sth.executeQuery();

                if (resultSet.next()) {
                    return (resultSet.getBoolean(1));
                }
            }
            return false;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public long getList(String name) throws UnknownList {
        try {
            try (PreparedStatement sth = connection.prepareStatement(
                    "SELECT id FROM cmdrListInfo " +
                            " WHERE name == ? ")) {
                sth.setString(1, name);
                ResultSet resultSet = sth.executeQuery();

                if (resultSet.next()) {
                    return (resultSet.getLong(1));
                }
            }
            throw new UnknownList();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateListHidden(long listId, boolean isHidden)
    {
        try (Connection writer = getWriteConnection()) {

            PreparedStatement sth;
            sth = writer.prepareStatement(new StringBuilder()
                    .append("UPDATE cmdrListInfo ")
                    .append(" SET hidden = ? ")
                    .append(" WHERE id == ?").toString());
            sth.setLong(2, listId);
            sth.setBoolean(1, isHidden);
            sth.execute();
            writer.commit();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateListPublic(long listId, boolean isPublic)
    {
        try (Connection writer = getWriteConnection()) {

            PreparedStatement sth;
            sth = writer.prepareStatement(new StringBuilder()
                    .append("UPDATE cmdrListInfo ")
                    .append(" SET public = ? ")
                    .append(" WHERE id == ?").toString());
            sth.setLong(2, listId);
            sth.setBoolean(1, isPublic);
            sth.execute();
            writer.commit();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ArrayList<Long> getSubscribed(long listId) {
        return getBooleanAccessMembers(SUBSCRIBER_TABLE, listId);
    }

    @Override
    public ArrayList<Long> getBlocked(long listId) {
        return getBooleanAccessMembers(BLOCKED_TABLE, listId);
    }

    @Override
    public ArrayList<Long> getPending(long listId) {
        return getBooleanAccessMembers(PENDING_TABLE, listId);
    }

    @Override
    public void updateListAccess(long listId, long userId, ListSubscription access) {
        if (access == ListSubscription.SUBSCRIBED) {
            setBooleanAccess(SUBSCRIBER_TABLE, listId, userId, true);

            setBooleanAccess(BLOCKED_TABLE, listId, userId, false);
            setBooleanAccess(PENDING_TABLE, listId, userId, false);
        }
        if (access == ListSubscription.BLOCKED) {
            setBooleanAccess(BLOCKED_TABLE, listId, userId, true);

            setBooleanAccess(SUBSCRIBER_TABLE, listId, userId, false);
            setBooleanAccess(PENDING_TABLE, listId, userId, false);
            setAdmin(listId, userId, false);
        }

        if (access == ListSubscription.REQUESTED) {
            setBooleanAccess(PENDING_TABLE, listId, userId, true);
        }

        if (access == ListSubscription.FORGET) {
            for(String tableName: BOOLEAN_TABLES) {
                setBooleanAccess(tableName, listId, userId, false);
            }
        }
    }
}
