package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import dataaccess.interfaces.AuthDAO;
import dataaccess.interfaces.GameDAO;
import dataaccess.interfaces.UserDAO;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import static java.sql.Types.NULL;

public class MySqlDAO {
    private int lastPID;
    private final Gson serializer;

    public MySqlDAO() {
        lastPID = 0;
        serializer = new Gson();
    }

    public class MySqlUserDAO extends MySqlDAO implements UserDAO {

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
        public void createUser(UserData u) throws DataAccessException {
            String create = "INSERT INTO user (name, pass, email) VALUES (?,?,?);";
            try {
                sendFlexCommand(create, u.username(), protect(u.password()), u.email());
            } catch (DataAccessException ex) {
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
            clearHelper("user");
        }

        @Override
        public int getSize() throws DataAccessException {
            return getSizeHelper("user");
        }

        private UserData analyzeGetResponse(PreparedStatement prep) throws SQLException {
            try (var rs = prep.executeQuery()) {
                if (rs.next()) {
                    return readUser(rs);
                }
            }
            return null;
        }

        private UserData readUser(ResultSet rs) throws SQLException {
            return new UserData(rs.getString("name"), rs.getString("pass"), rs.getString("email"));
        }
    }

    public class MySqlAuthDAO extends MySqlDAO implements AuthDAO {

        public MySqlAuthDAO() throws DataAccessException {
            String[] createAuthTableStatement = {
            """
            CREATE TABLE IF NOT EXISTS auth (
              token varchar(50) NOT NULL UNIQUE,
              name varchar(256) NOT NULL,
              PRIMARY KEY (token)
            );
            """
            };
            configureDatabase(createAuthTableStatement);
        }

        @Override
        public boolean createAuth(AuthData a) throws DataAccessException {
            String create = "INSERT INTO auth (token, name) VALUES (?,?);";
            try {
                sendFlexCommand(create, a.authToken(), a.username());
            } catch (DataAccessException ex) {
                if (ex.getMessage().startsWith("Duplicate", 27)) {
                    return true;
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
        public void deleteAuth(String token) throws DataAccessException {
            if (getAuth(token) == null) {
                throw new InvalidAuthorizationException("Invalid Authorization");
            }
            String delete = "DELETE FROM auth WHERE token=?;";
            sendFlexCommand(delete, token);
        }

        @Override
        public void clear() throws DataAccessException {
            clearHelper("auth");
        }

        @Override
        public int getSize() throws DataAccessException {
            return getSizeHelper("auth");
        }

        private AuthData analyzeGetResponse(PreparedStatement prep) throws SQLException {
            try (var rs = prep.executeQuery()) {
                if (rs.next()) {
                    return readAuth(rs);
                }
            }
            return null;
        }

        private AuthData readAuth(ResultSet rs) throws SQLException {
            return new AuthData(rs.getString("token"), rs.getString("name"));
        }
    }

    public class MySqlGameDAO extends MySqlDAO implements GameDAO {

        public MySqlGameDAO() throws DataAccessException {
            super();
            String[] createGameTableStatement = {
            """
            CREATE TABLE IF NOT EXISTS game (
              id int NOT NULL AUTO_INCREMENT,
              gameID int NOT NULL UNIQUE,
              wUser varchar(50),
              bUser varchar(256),
              gName varchar(256) NOT NULL,
              gData JSON NOT NULL,
              PRIMARY KEY (id),
              INDEX (gameID)
            );
            """
            };
            configureDatabase(createGameTableStatement);
        }

        public void createGame(GameData game) throws DataAccessException {
            String create = "INSERT INTO game (gameID, wUser, bUser, gName, gData) VALUES (?,?,?,?,?);";
            sendFlexCommand(create, game.gameID(), game.whiteUsername(), game.blackUsername(), game.gameName(), game.game());
        }

        public GameData getGame(int gameID) throws DataAccessException {
            String get = "SELECT gameID, wUser, bUser, gName, gData FROM game WHERE gameID=?;";
            try (var conn = DatabaseManager.getConnection()) {
                try (var prep = conn.prepareStatement(get)) {
                    prep.setInt(1, gameID);
                    GameData[] response = analyzeGetResponse(prep);
                    if (response.length == 0) {
                        return null;
                    } else {
                        return response[0];
                    }
                }
            } catch (SQLException ex) {
                throw new DataAccessException(String.format("Unable to access database: %s", ex.getMessage()));
            }
        }

        public GameData[] listGames() throws DataAccessException {
            String get = "SELECT gameID, wUser, bUser, gName, gData FROM game";
            try (var conn = DatabaseManager.getConnection()) {
                try (var prep = conn.prepareStatement(get)) {
                    return analyzeGetResponse(prep);
                }
            } catch (SQLException ex) {
                throw new DataAccessException(String.format("Unable to access database: %s", ex.getMessage()));
            }
        }

        public void updateGame(int gameID, GameData game) throws DataAccessException {
            String get = "UPDATE game SET wUser=(?), bUser=(?), gName=(?), gData=(?) WHERE gameID=(?);";
            try (var conn = DatabaseManager.getConnection()) {
                try (var prep = conn.prepareStatement(get)) {
                    prep.setString(1, game.whiteUsername());
                    prep.setString(2, game.blackUsername());
                    prep.setString(3, game.gameName());
                    prep.setString(4, serializer.toJson(game.game()));
                    prep.setInt(5, gameID);
                    prep.executeUpdate();
                    var success = prep.executeQuery("SELECT ROW_COUNT();");
                    if (!success.next()) {
                        throw new DataAccessException("Unable to access database");
                    }
                    if (success.getInt(1) != 1) {
                        throw new DataAccessException(String.format("Single update failed in database: %s", success.getInt(1)));
                    }
                }
            } catch (SQLException ex) {
                throw new DataAccessException(String.format("Unable to access database: %s", ex.getMessage()));
            }
        }

        public int getLastID() throws DataAccessException {
            String last = "SELECT gameID from game WHERE id=(?);";
            try (var conn = DatabaseManager.getConnection()) {
                try (var prep = conn.prepareStatement(last)) {
                    prep.setInt(1, lastPID);
                    var rs = prep.executeQuery();
                    if (rs.next()) {
                        return rs.getInt(1);
                    } else {
                        return 0;
                    }
                }
            } catch (SQLException ex) {
                throw new DataAccessException(String.format("Unable to access database: %s", ex.getMessage()));
            }
        }

        @Override
        public void clear() throws DataAccessException {
            clearHelper("game");
        }

        @Override
        public int getSize() throws DataAccessException {
            return getSizeHelper("game");
        }

        private GameData[] analyzeGetResponse(PreparedStatement prep) throws SQLException {
            ArrayList<GameData> games = new ArrayList<>();
            try (var rs = prep.executeQuery()) {
                while (rs.next()) {
                    games.add(readGame(rs));
                }
            }
            return games.toArray(new GameData[0]);
        }

        private GameData readGame(ResultSet rs) throws SQLException {
            ChessGame game = serializer.fromJson(rs.getString("gData"), ChessGame.class);
            return new GameData(rs.getInt("gameID"), rs.getString("wUser"), rs.getString("bUser"), rs.getString("gName"), game);
        }
    }


    public MySqlUserDAO getUserDAO() throws DataAccessException {
        return new MySqlUserDAO();
    }

    public MySqlAuthDAO getAuthDAO() throws DataAccessException {
        return new MySqlAuthDAO();
    }

    public MySqlGameDAO getGameDAO() throws DataAccessException {
        return new MySqlGameDAO();
    }


    private int getSizeHelper(String table) throws DataAccessException {
        String get = "SELECT COUNT(*) FROM " + table + ";";
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

    private void clearHelper(String table) throws DataAccessException {
        String stat = "TRUNCATE TABLE " + table;
        sendSetCommand(stat);
    }

    private void sendFlexCommand(String str, Object... params) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try (var prep = conn.prepareStatement(str)) {
                for (int i = 1; i <= params.length; i++) {
                    var par = params[i-1];
                    if (par instanceof String spar) {prep.setString(i, spar);}
                    else if (par instanceof Integer ipar) {prep.setInt(i, ipar);}
                    else if (par != null) {prep.setString(i, serializer.toJson(par));}
                    else {prep.setNull(i, NULL);}
                }
                prep.executeUpdate();
                var rs = prep.executeQuery("SELECT LAST_INSERT_ID()");
                if (rs.next()) {
                    lastPID = rs.getInt(1);
                }
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

    private String protect(String pass) {
        return BCrypt.hashpw(pass, BCrypt.gensalt());
    }
}
