package client;

import dataaccess.DataAccessException;
import dataaccess.MySqlDAO;
import dataaccess.interfaces.AuthDAO;
import dataaccess.interfaces.GameDAO;
import dataaccess.interfaces.UserDAO;
import exception.ResponseException;

import model.*;
import org.junit.jupiter.api.*;

import server.Server;
import server.ServerFacade;

import result.*;
import request.*;

import static org.junit.jupiter.api.Assertions.*;

public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade facade;

    private static UserDAO uDAO;
    private static AuthDAO aDAO;
    private static GameDAO gDAO;

    private static UserData user;
    private static RegisterRequest reg;

    @BeforeAll
    public static void init() throws DataAccessException {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade("http://localhost:" + port);

        dataaccess.MySqlDAO overDAO = new MySqlDAO();
        uDAO = overDAO.getUserDAO();
        aDAO = overDAO.getAuthDAO();
        gDAO = overDAO.getGameDAO();

        user = new UserData("TestUser", "fakePass", "cheese@is");
        reg = new RegisterRequest(user.username(), user.password(), user.email());
    }

    @BeforeEach
    void prep() throws DataAccessException {
        clear();
    }

    @AfterAll
    static void stopServer() throws DataAccessException {
        server.stop();
        clear();
    }


    @Test
    public void positiveRegister() throws ResponseException, DataAccessException {
        RegisterResult result = facade.register(reg);
        UserData actual = uDAO.getUser(reg.username());
        assertNotNull(result);
        assertEquals(result.username(), actual.username());
        assertNotNull(result.authToken());
        aDAO.getAuth(result.authToken());
    }

    @Test
    public void negativeRegister() throws ResponseException, DataAccessException {
        facade.register(reg);
        assertNotNull(uDAO.getUser(reg.username()));
        assertThrows(ResponseException.class, () -> facade.register(reg));
    }

    @Test
    public void positiveLogin() throws ResponseException, DataAccessException {
        RegisterResult result = facade.register(reg);
        assertNotNull(result);
        LoginResult login = facade.login(new LoginRequest(reg.username(), reg.password()));
        assertNotNull(login);
        assertNotNull(login.authToken());
        assertNotNull(aDAO.getAuth(login.authToken()));
    }

    @Test
    public void negativeLogin() {
        assertThrows(ResponseException.class, () -> facade.login(new LoginRequest(reg.username(), reg.password())));
    }

    @Test
    public void positiveLogout() throws ResponseException, DataAccessException {
        RegisterResult result = facade.register(reg);
        assertNotNull(result.authToken());
        assertNotNull(aDAO.getAuth(result.authToken()));
        LogoutResult logout = facade.logout(result.authToken());
        assertNull(aDAO.getAuth(result.authToken()));
        assertNotNull(logout);
    }

    @Test
    public void negativeLogout() throws ResponseException {
        facade.register(reg);
        assertThrows(ResponseException.class, () -> facade.logout("completely fake authorization code..."));
    }

    @Test
    public void positiveCreateGame() throws ResponseException, DataAccessException {
        RegisterResult result = facade.register(reg);
        assertNotNull(result);
        NewGameResult gameResult = facade.createGame(new CreateRequest("testGame"), result.authToken());
        assertNotNull(gameResult);
        assertNotNull(gDAO.getGame(gameResult.gameID()));
    }

    @Test
    public void negativeCreateGame() {
        assertThrows(ResponseException.class, () -> facade.createGame(new CreateRequest("testGame"), "open this door!"));
    }

    @Test
    public void positiveListGames() throws ResponseException {
        RegisterResult result = facade.register(reg);
        assertNotNull(result);
        facade.createGame(new CreateRequest("testGame"), result.authToken());
        facade.createGame(new CreateRequest("testGame1"), result.authToken());
        ListResult list = facade.listGames(result.authToken());
        assertNotNull(list);
        assertEquals("testGame", list.getArray().getFirst().gameName());
        assertEquals("testGame1", list.getArray().getLast().gameName());
    }

    @Test
    public void negativeListGames() throws ResponseException {
        facade.register(reg);
        assertThrows(ResponseException.class, () -> facade.listGames("Not even close!"));
    }

    @Test
    public void positiveJoinGame() throws ResponseException, DataAccessException {
        RegisterResult result = facade.register(reg);
        assertNotNull(result);
        NewGameResult game = facade.createGame(new CreateRequest("testGame"), result.authToken());
        assertNotNull(game);
        JoinResult join = facade.joinGame(new JoinRequest("BLACK", game.gameID()), result.authToken());
        assertNotNull(join);
        GameData instantiatedGame = gDAO.getGame(game.gameID());
        assertEquals(user.username(), instantiatedGame.blackUsername());
    }

    @Test
    public void negativeJoinGame() {
        assertThrows(ResponseException.class, () -> facade.joinGame(new JoinRequest("BLACK", 1), "Frying Pans!"));
    }

    @Test
    public void positiveClear() throws ResponseException, DataAccessException {
        RegisterResult result = facade.register(reg);
        assertNotNull(result);
        NewGameResult game = facade.createGame(new CreateRequest("testGame"), result.authToken());
        assertNotNull(game);
        JoinResult join = facade.joinGame(new JoinRequest("BLACK", game.gameID()), result.authToken());
        assertNotNull(join);
        facade.clear();
        assertThrows(ResponseException.class, () -> facade.login(new LoginRequest(user.username(), user.password())));
        assertEquals(0, aDAO.getSize());
        assertEquals(0, uDAO.getSize());
        assertEquals(0, gDAO.getSize());
    }

    public static void clear() throws DataAccessException {
        uDAO.clear();
        aDAO.clear();
        gDAO.clear();
    }
}
