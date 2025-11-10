package client;

import dataaccess.DataAccessException;
import dataaccess.MySqlDAO;
import dataaccess.interfaces.AuthDAO;
import dataaccess.interfaces.GameDAO;
import dataaccess.interfaces.UserDAO;
import exception.ResponseException;
import model.UserData;
import org.junit.jupiter.api.*;

import server.Server;
import server.ServerFacade;

import static org.junit.jupiter.api.Assertions.*;

public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade facade;

    private static UserDAO uDAO;
    private static AuthDAO aDAO;
    private static GameDAO gDAO;

    private static UserData user;

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
    }

    @AfterAll
    static void stopServer() throws DataAccessException {
        server.stop();
        clear();
    }


    @Test
    public void positiveRegister() throws ResponseException, DataAccessException {
        var result = facade.register(user);
        var actual = uDAO.getUser(user.username());
        assertEquals(result, actual);
    }

    public static void clear() throws DataAccessException {
        uDAO.clear();
        aDAO.clear();
        gDAO.clear();
    }
}
