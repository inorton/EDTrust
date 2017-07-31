package space.edhits.edtrust.data;

import org.sqlite.SQLiteConnection;
import space.edhits.edtrust.UnknownUser;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Created by inb on 30/07/2017.
 */
public class SQLiteUserProfileData extends SQLiteDataSource implements UserProfileData {

    public SQLiteUserProfileData(String url) {
        super(url);
    }

    @Override
    protected void makeTables() throws SQLException {
        try (Statement sth = connection.createStatement()) {
            String profiles = new StringBuilder()
                    .append("CREATE TABLE IF NOT EXISTS userProfiles (")
                    .append(" id INTEGER PRIMARY KEY, ")
                    .append(" apikey TEXT UNIQUE, ")
                    .append(" email TEXT UNIQUE, ")
                    .append(" admin INTEGER )").toString();
            sth.execute(profiles);
        }
        try (Statement sth = connection.createStatement()) {
            String subs = new StringBuilder()
                    .append("CREATE TABLE IF NOT EXISTS userSubscriptions (")
                    .append(" user INTEGER, ")
                    .append(" list INTEGER )").toString();
            sth.execute(subs);
        }
        try (Statement sth = connection.createStatement()) {
            String index = new StringBuilder()
                    .append("CREATE INDEX IF NOT EXISTS I_userSubscriptions")
                    .append(" ON userSubscriptions ")
                    .append(" (user, list) ").toString();
            sth.execute(index);
        }
    }

    private static String makeSelectQuery(String from, List<String> cols, String where) {
        String colnames = String.join(", ", cols);

        StringBuilder sb = new StringBuilder()
                .append(String.format("SELECT %s FROM %s ", colnames, from))
                .append(String.format("WHERE %s == ?", where));

        return sb.toString();
    }

    static <T> PreparedStatement prepareSelectGeneric(Connection connection,
                                                      String from,
                                                      List<String> cols,
                                                      String where,
                                                      T value)
    {
        String query = makeSelectQuery(from, cols, where);

        try {
            PreparedStatement sth = connection.prepareStatement(query);
            sth.clearParameters();

            if (value instanceof String) {
                sth.setString(1, String.valueOf(value));
            }
            if (value instanceof Long) {
                sth.setLong(1, (Long)value);
            }
            return sth;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public long getIdFromKey(String apikey) throws UnknownUser
    {
        try (PreparedStatement sth = prepareSelectGeneric(connection,
                "userProfiles",
                Arrays.asList("id"), "apikey", apikey)) {
            ResultSet resultSet = sth.executeQuery();
            if (resultSet.next()) {
                return resultSet.getLong("id");
            }
        } catch (SQLException err ){
            throw new RuntimeException(err);
        }
        throw new UnknownUser();
    }

    @Override
    public long getId(String email) throws UnknownUser {
        try (PreparedStatement sth = prepareSelectGeneric(connection,
                "userProfiles",
                Arrays.asList("id"), "email", email)) {
            ResultSet resultSet = sth.executeQuery();
            if (resultSet.next()) {
                return resultSet.getLong("id");
            }
        } catch (SQLException err ){
            throw new RuntimeException(err);
        }
        throw new UnknownUser();
    }

    @Override
    public String getApiKey(long userId) {
        try (PreparedStatement sth = prepareSelectGeneric(connection,
                "userProfiles",
                Arrays.asList("apikey"), "id", userId)) {
            ResultSet resultSet = sth.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("apikey");
            }
        } catch (SQLException err ){
            throw new RuntimeException(err);
        }
        return null;
    }

    @Override
    public Boolean getAdminStatus(long userId) {

        try (PreparedStatement sth = prepareSelectGeneric(connection,
                "userProfiles",
                Arrays.asList("admin"), "id", Long.valueOf(userId))) {
            ResultSet resultSet = sth.executeQuery();
            if (resultSet.next()) {
                return resultSet.getBoolean("admin");
            }
        } catch (SQLException err ){
            throw new RuntimeException(err);
        }
        return false;
    }

    @Override
    public String getEmail(long userId) throws UnknownUser {

        try (PreparedStatement sth = prepareSelectGeneric(connection,
                "userProfiles",
                Arrays.asList("email"), "id", Long.valueOf(userId))) {
            ResultSet resultSet = sth.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("email");
            }
        } catch (SQLException err ){
            throw new RuntimeException(err);
        }
        throw new UnknownUser();
    }

    @Override
    public long makeProfile(String email) {
        try {
            try (Connection writer = getWriteConnection()) {
                String sql = new StringBuilder()
                        .append("INSERT INTO userProfiles ")
                        .append(" (apikey, email, admin) ")
                        .append(" VALUES( ?, ?, ?)").toString();
                PreparedStatement sth = writer.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                sth.setString(1, UUID.randomUUID().toString());
                sth.setString(2, email);
                sth.setBoolean(3, false);
                sth.execute();
                writer.commit();

                ResultSet generatedKeys = sth.getGeneratedKeys();
                if (generatedKeys.next()){
                    return generatedKeys.getLong(1);
                } else {
                    throw new RuntimeException("could not create profile");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateProfileAdmin(long userId, boolean adminStatus) {
        try {
            try (Connection writer = getWriteConnection()) {
                String sql = new StringBuilder()
                        .append("UPDATE userProfiles ")
                        .append(" SET admin = ? ")
                        .append(" WHERE id = ?").toString();
                PreparedStatement sth = writer.prepareStatement(sql);
                sth.setBoolean(1, adminStatus);
                sth.setLong(2, userId);
                sth.execute();
                writer.commit();
            }
        } catch (SQLException err) {
            throw new RuntimeException(err);
        }
    }

    @Override
    public ArrayList<Long> getSubscriptions(long userId) {
        ArrayList<Long> found = new ArrayList<>();

        try (PreparedStatement sth = prepareSelectGeneric(connection,
                "userSubscriptions",
                Arrays.asList("list"), "user", userId)) {
            ResultSet resultSet = sth.executeQuery();
            while (resultSet.next()) {
                long list = resultSet.getLong("list");
                found.add(list);
            }
            return found;
        } catch (SQLException err ){
            throw new RuntimeException(err);
        }
    }

    @Override
    public void addSubscription(long userId, long listId) {
        try {
            try (Connection writer = getWriteConnection()) {
                String sql = new StringBuilder()
                        .append("REPLACE INTO userSubscriptions ")
                        .append(" (user, list) ")
                        .append(" VALUES( ?, ?)").toString();
                PreparedStatement sth = writer.prepareStatement(sql);
                sth.setLong(1, userId);
                sth.setLong(2, listId);
                sth.execute();
                writer.commit();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void removeSubscription(long userId, long listId) {
        try {
            try (Connection writer = getWriteConnection()) {
                String sql = new StringBuilder()
                        .append("DELETE FROM userSubscriptions ")
                        .append("WHERE user == ? AND list == ?").toString();
                PreparedStatement sth = writer.prepareStatement(sql);
                sth.setLong(1, userId);
                sth.setLong(2, listId);
                sth.execute();
                writer.commit();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
