package dataaccess;

import dataaccess.interfaces.AuthDAO;
import dataaccess.interfaces.GameDAO;
import dataaccess.interfaces.UserDAO;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.xml.crypto.Data;
import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DataaccessTests {

    private static UserDAO uDAO;
    private static AuthDAO aDAO;
    private static GameDAO gDAO;

    private static UserData user;
    private static AuthData auth;

    @BeforeAll
    static void setUP() throws DataAccessException {
        uDAO = new MySqlUserDAO();
        aDAO = new MySqlAuthDAO();
//            gDAO = new MemoryGameDAO();
        user = new UserData("testUser", "testPassword", "");
        auth = new AuthData("fakeAuthTokenYippee", "testUser");
    }

    @BeforeEach
    void clear() {
        try {
            uDAO.clear();
            aDAO.clear();
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
//            aDAO.clear();
//            gDAO.clear();
    }

    @Test
    public void positiveCreateUser() throws AlreadyTakenException, DataAccessException {
        uDAO.createUser(user);
        String retrieveStat = "SELECT name, pass, email FROM user WHERE name='" + user.username() + "';";
        var response = sendQueryCommand(retrieveStat, new Object[][]{{"name", "s"}, {"pass", "s"}, {"email", "s"}});
        UserData retroUser = new UserData((String)response.get(0), (String)response.get(1), (String)response.get(2));
        assertEquals(user, retroUser);
    }

    @Test
    public void negativeCreateUser() throws AlreadyTakenException, DataAccessException {
        uDAO.createUser(user);
        assertThrows(AlreadyTakenException.class, () -> uDAO.createUser(user));
    }

    @Test
    public void positiveGetUser() throws DataAccessException {
        uDAO.createUser(user);
        assertEquals(user, uDAO.getUser(user.username()));
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


//        @Test
//        void positiveClear() {
//
//        }

    static private ArrayList<Object> sendQueryCommand(String str, Object[][] params) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            var sql = conn.prepareStatement(str);
            ArrayList<Object> collect = new ArrayList<>() {};
            ResultSet response = sql.executeQuery();
            while (response.next()) {
                for (int i = 1; i <= params.length; i++) {
                    var par = params[i - 1];
                    if (par[1] instanceof String _) {collect.add(response.getString((String)par[0]));}
                    else if (par[1] instanceof Integer _) {collect.add(response.getInt((Integer)par[0]));}
                }
            }
            return collect;
        } catch (SQLException ex) {
            throw new DataAccessException(String.format("Unable to connect database: %s", ex.getMessage()));
        }
    }
}
