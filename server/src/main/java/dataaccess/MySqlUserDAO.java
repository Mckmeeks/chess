package dataaccess;

import dataaccess.interfaces.UserDAO;
import model.UserData;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MySqlUserDAO implements UserDAO {

    public MySqlUserDAO() throws DataAccessException {
        String[] createUserTableStatement = {
                """
        CREATE TABLE IF NOT EXISTS user (
          id int NOT NULL AUTO_INCREMENT,
          name varchar(50) NOT NULL UNIQUE,
          pass varchar(256) NOT NULL,
          email varchar(256),
          PRIMARY KEY (id),
          INDEX (name)
        );
        """
        };
        configureDatabase(createUserTableStatement);
    }

    @Override
    public void createUser(UserData u) throws AlreadyTakenException, DataAccessException {
        String create = "INSERT INTO user (name, pass, email) VALUES (?,?,?);";
        try {sendFlexCommand(create, u.username(), u.password(), u.email());}
        catch (DataAccessException ex) {
            if (ex.getMessage().startsWith("Duplicate", 27)) {
                throw new AlreadyTakenException(String.format("Unable to update database: %s", ex.getMessage()));
            }
            throw ex;
        }
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        String get = "SELECT name, pass, email FROM user WHERE name=?;";
        try (var conn = DatabaseManager.getConnection()) {
            try (var prep = conn.prepareStatement(get)) {
                prep.setString(1, username);
                return analyzeGetResponse(prep);
            }
        } catch (SQLException ex) {
            throw new DataAccessException(String.format("Unable to access database: %s", ex.getMessage()));
        }
    }

    @Override
    public void clear() throws DataAccessException {
        String stat = "TRUNCATE TABLE user";
        sendSetCommand(stat);
    }

    @Override
    public int getSize() throws DataAccessException {
        String get = "SELECT COUNT(*) FROM user;";
        try (var conn = DatabaseManager.getConnection()) {
            try (var prep = conn.prepareStatement(get)) {
                return analyzeCountResponse(prep);
            }
        } catch (SQLException ex) {
            throw new DataAccessException(String.format("Unable to access database: %s", ex.getMessage()));
        }
    }

    private int analyzeCountResponse(PreparedStatement prep) throws SQLException {
        try (var rs = prep.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return -1;
    }

    private UserData analyzeGetResponse(PreparedStatement prep) throws SQLException {
        try (var rs = prep.executeQuery()) {
            if (rs.next()) {return readUser(rs);}
        }
        return null;
    }

    private UserData readUser(ResultSet rs) throws SQLException {
        return new UserData(rs.getString("name"), rs.getString("pass"), rs.getString("email"));
    }

    private void sendFlexCommand(String str, Object... params) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try (var prep = conn.prepareStatement(str)) {
                for (int i = 1; i <= params.length; i++) {
                    var par = params[i-1];
                    if (par instanceof String spar) {prep.setString(i, spar);}
                    else if (par instanceof Integer ipar) {prep.setInt(i, ipar);}
                }
                prep.executeUpdate();
            }
            conn.commit();
        } catch (SQLException ex) {
            throw new DataAccessException(String.format("Unable to update database: %s", ex.getMessage()));
        }
    }

    private void sendSetCommand(String str) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            var sql = conn.prepareStatement(str);
            sql.executeUpdate();
            conn.commit();
        } catch (SQLException ex) {
            throw new DataAccessException(String.format("Unable to update database: %s", ex.getMessage()));
        }
    }

    private void configureDatabase(String[] configuration) throws DataAccessException {
        DatabaseManager.createDatabase();
        try (var conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            var sql = conn.createStatement();
            for (String stat : configuration) {
                sql.addBatch(stat);
            }
            sql.executeBatch();
            conn.commit();
        } catch (SQLException ex) {
            throw new DataAccessException(String.format("Unable to configure database: %s", ex.getMessage()));
        }
    }
}
