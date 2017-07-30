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
public class SQLiteUserProfileData implements UserProfileData {

    private String url;
    Connection connection;

    public SQLiteUserProfileData(String url) {
        this.url = url;
        this.init();
    }

    /**
     * Get a connection set in transaction mode.
     * @return
     * @throws SQLException
     */
    private Connection getWriteConnection() throws SQLException {
        Connection c = DriverManager.getConnection(this.url);
        c.setAutoCommit(false);
        return c;
    }

    protected void init() {
        try {
            connection = DriverManager.getConnection(this.url);
            this.makeTables();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

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
    public void close() throws IOException {
        try {
            this.connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
