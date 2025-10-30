package dataaccess;

import dataaccess.interfaces.AuthDAO;
import model.AuthData;
import model.UserData;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MySqlAuthDAO implements AuthDAO {
    public MySqlAuthDAO() throws DataAccessException {
        String[] createUserTableStatement = {
                """
        CREATE TABLE IF NOT EXISTS auth (
          token varchar(50) NOT NULL UNIQUE,
          name varchar(256) NOT NULL,
          PRIMARY KEY (token)
        );
        """
        };
        configureDatabase(createUserTableStatement);
    }
    @Override
    public boolean createAuth(AuthData a) throws DataAccessException {
        String create = "INSERT INTO auth (token, name) VALUES (?,?);";
        try {sendFlexCommand(create, a.authToken(), a.username());}
        catch (DataAccessException ex) {
            if (ex.getMessage().startsWith("Duplicate", 27)) {
                return true;
//                throw new AlreadyTakenException(String.format("Unable to update database: %s", ex.getMessage()));
            }
            throw ex;
        }
        return false;
    }

    @Override
    public AuthData getAuth(String token) throws DataAccessException {
        String get = "SELECT token, name FROM auth WHERE token=?;";
        try (var conn = DatabaseManager.getConnection()) {
            try (var prep = conn.prepareStatement(get)) {
                prep.setString(1, token);
                return analyzeGetResponse(prep);
            }
        } catch (SQLException ex) {
            throw new DataAccessException(String.format("Unable to access database: %s", ex.getMessage()));
        }
    }

    @Override
    public void deleteAuth(String token) throws InvalidAuthorizationException, DataAccessException {
        if (getAuth(token) == null) {throw new InvalidAuthorizationException("Invalid Authorization");}
        String delete = "DELETE FROM auth WHERE token=?;";
        sendFlexCommand(delete, token);
    }

    @Override
    public void clear() throws DataAccessException {
        String stat = "TRUNCATE TABLE auth";
        sendSetCommand(stat);
    }

    @Override
    public int getSize() throws DataAccessException {
        String get = "SELECT COUNT(*) FROM auth;";
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

    private AuthData analyzeGetResponse(PreparedStatement prep) throws SQLException {
        try (var rs = prep.executeQuery()) {
            if (rs.next()) {return readAuth(rs);}
        }
        return null;
    }

    private AuthData readAuth(ResultSet rs) throws SQLException {
        return new AuthData(rs.getString("token"), rs.getString("name"));
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
