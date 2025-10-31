package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import dataaccess.interfaces.AuthDAO;
import dataaccess.interfaces.GameDAO;
import dataaccess.interfaces.UserDAO;
import model.AuthData;
import model.GameData;
import model.UserData;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class DataaccessTests {

    private static UserDAO uDAO;
    private static AuthDAO aDAO;
    private static GameDAO gDAO;

    private static UserData user;
    private static AuthData auth;
    private static GameData game;

    static Gson serializer;

    @BeforeAll
    static void setUP() throws DataAccessException {
        uDAO = new MySqlUserDAO();
        aDAO = new MySqlAuthDAO();
        gDAO = new MySqlGameDAO();
        user = new UserData("testUser", "testPassword", "");
        auth = new AuthData("fakeAuthTokenYippee", "testUser");
        game = new GameData(1, "white", "black", "bond and free", new ChessGame());
        serializer = new Gson();
    }

    @BeforeEach
    void clear() {
        try {
            uDAO.clear();
            aDAO.clear();
            gDAO.clear();
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void positiveCreateUser() throws AlreadyTakenException, DataAccessException {
        uDAO.createUser(user);
        String retrieveStat = "SELECT name, pass, email FROM user WHERE name='" + user.username() + "';";
        var response = sendQueryCommand(retrieveStat, new Object[][]{{"name", "s"}, {"pass", "s"}, {"email", "s"}});
        UserData retroUser = new UserData((String)response.get(0), (String)response.get(1), (String)response.get(2));
        compareUserEnc(user, retroUser);
    }

    @Test
    public void negativeCreateUser() throws AlreadyTakenException, DataAccessException {
        uDAO.createUser(user);
        assertThrows(AlreadyTakenException.class, () -> uDAO.createUser(user));
    }

    @Test
    public void positiveGetUser() throws DataAccessException {
        uDAO.createUser(user);
        compareUserEnc(user, uDAO.getUser(user.username()));
    }

    @Test
    public void negativeGetUser() throws DataAccessException {
        uDAO.createUser(user);
        assertNull(uDAO.getUser("fakeUser"));
    }

    @Test
    public void positiveGetUserSize() throws DataAccessException {
        uDAO.createUser(user);
        uDAO.createUser(new UserData("userTwo", "password", ""));
        assertEquals(2, uDAO.getSize());
    }

    @Test
    public void positiveGetUserSizeNone() throws DataAccessException {
        assertEquals(0, uDAO.getSize());
    }


    @Test
    public void positiveCreateAuth() throws DataAccessException {
        aDAO.createAuth(auth);
        String retrieveStat = "SELECT token, name FROM auth WHERE token='" + auth.authToken() + "';";
        var response = sendQueryCommand(retrieveStat, new Object[][]{{"token", "s"}, {"name", "s"}});
        AuthData retroAuth = new AuthData((String)response.get(0), (String)response.get(1));
        assertEquals(auth, retroAuth);
        aDAO.createAuth(new AuthData("NewME", "Name"));
    }

    @Test
    public void negativeCreateAuth() throws DataAccessException {
        aDAO.createAuth(auth);
        assertTrue(aDAO.createAuth(auth));
    }

    @Test
    public void positiveGetAuth() throws DataAccessException {
        aDAO.createAuth(auth);
        assertEquals(auth, aDAO.getAuth(auth.authToken()));
    }

    @Test
    public void negativeGetAuth() throws DataAccessException {
        aDAO.createAuth(auth);
        assertNull(aDAO.getAuth("silly, I don't exist!"));
    }

    @Test
    public void positiveDeleteAuth() throws DataAccessException {
        aDAO.createAuth(auth);
        aDAO.deleteAuth(auth.authToken());
        assertNull(aDAO.getAuth(auth.authToken()));
    }

    @Test
    public void negativeDeleteAuth() throws DataAccessException {
        aDAO.clear();
        assertThrows(InvalidAuthorizationException.class, () -> aDAO.deleteAuth(auth.authToken()));
    }

    @Test
    public void positiveGetAuthSize() throws DataAccessException {
        aDAO.createAuth(auth);
        aDAO.createAuth(new AuthData("Secondary", "person"));
        assertEquals(2, aDAO.getSize());
    }


    @Test
    public void positiveCreateGame() throws DataAccessException {
        gDAO.createGame(game);
        String retrieveStat = "SELECT gameID, wUser, bUser, gName, gData FROM game WHERE gameID='" + game.gameID() + "';";
        var response = sendQueryCommand(retrieveStat, new Object[][]{{"gameID", 1}, {"wUser", "s"}, {"bUser", "s"}, {"gName", "s"}, {"gData", "s"}});
        var gameObject = serializer.fromJson((String)response.get(4), ChessGame.class);
        GameData retroGame = new GameData((int)response.get(0), (String)response.get(1), (String)response.get(2), (String)response.get(3), gameObject);
        assertEquals(game, retroGame);
    }

    @Test
    public void negativeCreateGame() throws DataAccessException {
        gDAO.createGame(game);
        assertThrows(DataAccessException.class, () -> gDAO.createGame(game));
    }

    @Test
    public void positiveGetGame() throws DataAccessException {
        gDAO.createGame(game);
        assertEquals(game, gDAO.getGame(game.gameID()));
    }

    @Test
    public void negativeGetGame() throws DataAccessException {
        gDAO.createGame(game);
        assertNull(gDAO.getGame(-100));
    }

    @Test
    public void positiveGetSize() throws DataAccessException {
        gDAO.createGame(game);
        gDAO.createGame(new GameData(2,"","","cheese", new ChessGame()));
        assertEquals(2, gDAO.getSize());
    }

    @Test
    public void emptyGetSize() throws DataAccessException {
        gDAO.clear();
        assertEquals(0, gDAO.getSize());
    }

    @Test
    public void positiveListGames() throws DataAccessException {
        gDAO.createGame(game);
        gDAO.createGame(new GameData(2,"","","cheese", new ChessGame()));
        assertEquals(gDAO.listGames().length, gDAO.getSize());
    }

    @Test
    public void emptyListGames() throws DataAccessException {
        gDAO.clear();
        assertEquals(0, gDAO.listGames().length);
    }

    @Test
    public void positiveUpdateGame() throws DataAccessException {
        gDAO.createGame(game);
        var newGame = new GameData(1, "new", "New", game.gameName(), new ChessGame());
        gDAO.updateGame(1, newGame);
        GameData retroGame = gDAO.getGame(1);
        assertEquals(newGame, retroGame);
    }

    @Test
    public void negativeUpdateGame() throws DataAccessException {
        gDAO.createGame(game);
        assertThrows(DataAccessException.class, () -> gDAO.updateGame(4, game));
    }

    @Test
    public void positiveGetLastID() throws DataAccessException {
        gDAO.createGame(game);
        gDAO.createGame(new GameData(5, "", "", "", new ChessGame()));
        assertEquals(5, gDAO.getLastID());
    }

    @Test
    void positiveClear() throws DataAccessException {
        uDAO.createUser(user);
        aDAO.createAuth(auth);
        gDAO.createGame(game);
        compareUserEnc(user, uDAO.getUser(user.username()));
        assertEquals(auth, aDAO.getAuth(auth.authToken()));
        assertEquals(game, gDAO.getGame(game.gameID()));
        uDAO.clear();
        aDAO.clear();
        gDAO.clear();
        assertNull(uDAO.getUser(user.username()));
        assertNull(aDAO.getAuth(auth.authToken()));
        assertNull(gDAO.getGame(game.gameID()));

    }

    static private ArrayList<Object> sendQueryCommand(String str, Object[][] params) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            var sql = conn.prepareStatement(str);
            ArrayList<Object> collect = new ArrayList<>() {};
            ResultSet response = sql.executeQuery();
            while (response.next()) {
                for (int i = 1; i <= params.length; i++) {
                    var par = params[i - 1];
                    if (par[1] instanceof String _) {collect.add(response.getString((String)par[0]));}
                    else if (par[1] instanceof Integer _) {collect.add(response.getInt((String)par[0]));}
                }
            }
            return collect;
        } catch (SQLException ex) {
            throw new DataAccessException(String.format("Unable to connect database: %s", ex.getMessage()));
        }
    }

    static private void compareUserEnc(UserData user, UserData userEnc) {
        assertEquals(user.username(), userEnc.username());
        assertEquals(user.email(), userEnc.email());
        assertTrue(BCrypt.checkpw(user.password(), userEnc.password()));
    }
}
