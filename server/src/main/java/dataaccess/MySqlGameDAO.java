package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import dataaccess.interfaces.GameDAO;

import model.GameData;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import static java.sql.Types.NULL;

public class MySqlGameDAO implements GameDAO {

    private final Gson serializer;
    private int lastPID = 0;

    public MySqlGameDAO() throws DataAccessException {
        serializer = new Gson();
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
                if (response.length == 0) {return null;}
                else {return response[0];}
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
                if (!success.next()) throw new DataAccessException("Unable to access database");
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
                if (rs.next()) {return rs.getInt(1);}
                else {return 0;}
            }
        } catch (SQLException ex) {
            throw new DataAccessException(String.format("Unable to access database: %s", ex.getMessage()));
        }
    }

    @Override
    public void clear() throws DataAccessException {
        String stat = "TRUNCATE TABLE game";
        sendSetCommand(stat);
    }

    @Override
    public int getSize() throws DataAccessException {
        String get = "SELECT COUNT(*) FROM game;";
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

    private GameData[] analyzeGetResponse(PreparedStatement prep) throws SQLException {
        ArrayList<GameData> games = new ArrayList<>();
        try (var rs = prep.executeQuery()) {
            while (rs.next()) {games.add(readGame(rs));}
        }
        return games.toArray(new GameData[0]);
    }

    private GameData readGame(ResultSet rs) throws SQLException {
        ChessGame game = serializer.fromJson(rs.getString("gData"), ChessGame.class);
        return new GameData(rs.getInt("gameID"), rs.getString("wUser"), rs.getString("bUser"), rs.getString("gName"), game);
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



}
