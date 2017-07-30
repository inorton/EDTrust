package space.edhits.edtrust.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Created by inb on 30/07/2017.
 */
public abstract class SQLiteDataSource {

    private String url;
    Connection connection;

    public SQLiteDataSource(String url) {
        this.url = url;
        this.init();
    }

    /**
     * Get a connection set in transaction mode.
     * @return
     * @throws SQLException
     */
    protected Connection getWriteConnection() throws SQLException {
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

    protected abstract void makeTables() throws SQLException;

    public synchronized void close() {
        if (this.connection != null) {
            try {
                this.connection.close();
            } catch (SQLException e) {
            }
        }
        this.connection = null;
    }
}
