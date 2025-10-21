package service;

import static org.junit.jupiter.api.Assertions.*;
import io.javalin.http.BadRequestResponse;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import dataaccess.*;
import dataaccess.interfaces.*;

import model.*;
import handler.request.*;
import service.result.*;

public class ServiceUnitTests {
    private static UserDAO uDAO;
    private static AuthDAO aDAO;
    private static GameDAO gDAO;

    @BeforeAll
    static void setUP() {
        uDAO = new MemoryUserDAO();
        aDAO = new MemoryAuthDAO();
        gDAO = new MemoryGameDAO();
    }

    @BeforeEach
    void clear() {
        uDAO.clear();
        aDAO.clear();
        gDAO.clear();
    }

    static RegisterResult register() throws AlreadyTakenException {
        var request = new RegisterRequest("TestUser", "pass","");
        User userService = new User(uDAO, aDAO);
        return userService.register(request);
    }

    @Test
    void positiveRegister() throws AlreadyTakenException{
        var response = register();
        assertEquals("TestUser", response.username());
        assertNotNull(response.authToken());
    }

    @Test
    void negativeRegisterTwice() throws AlreadyTakenException {
        register();
        var request = new RegisterRequest("TestUser", "letMeIn","not.your@cheese.com");
        User userService = new User(uDAO, aDAO);
        assertThrows(DataAccessException.class, () -> userService.register(request));
    }

    @Test
    void positiveLogin() throws InvalidAuthorizationException, AlreadyTakenException {
        register();
        var request = new LoginRequest("TestUser", "pass");
        User userService = new User(uDAO, aDAO);
        LoginResult result = userService.login(request);
        assertNotNull(result.authToken());
        assertNotNull(aDAO.getAuth(result.authToken()));
    }

    @Test
    void negativeLoginInvalid() throws AlreadyTakenException {
        var request = new LoginRequest("TestUser", "pass");
        User userService = new User(uDAO, aDAO);
        assertThrows(InvalidAuthorizationException.class, () -> userService.login(request));
        register();
        var invalidRequest = new LoginRequest("TestUser", "invalid");
        assertThrows(InvalidAuthorizationException.class, () -> userService.login(invalidRequest));
    }

    @Test
    void positiveLogout() throws InvalidAuthorizationException, AlreadyTakenException {
        var registerResult = register();
        assertNotNull(aDAO.getAuth(registerResult.authToken()));
        User userService = new User(uDAO, aDAO);
        userService.logout(registerResult.authToken());
        assertNull(aDAO.getAuth(registerResult.authToken()));
    }

    @Test
    void negativeLogout() {
        User userService = new User(uDAO, aDAO);
        assertThrows(InvalidAuthorizationException.class, () -> userService.logout("badAuthToken"));
    }

    @Test
    void positiveNewGame() throws InvalidAuthorizationException, AlreadyTakenException {
        var registerResult = register();
        Game gameService = new Game(aDAO, gDAO);
        CreateRequest request = new CreateRequest("testGame");
        NewGameResult result = gameService.newGame(registerResult.authToken(), request);
        assertNotNull(gDAO.getGame(result.gameID()));
    }

    @Test
    void negativeNewGame() throws AlreadyTakenException {
        var registerResult = register();
        Game gameService = new Game(aDAO, gDAO);
        CreateRequest request = new CreateRequest("");
        assertThrows(BadRequestException.class, () -> gameService.newGame(registerResult.authToken(), request));
    }

    @Test
    void positiveListGames() throws InvalidAuthorizationException, AlreadyTakenException {
        var registerResult = register();
        Game gameService = new Game(aDAO, gDAO);
        CreateRequest request = new CreateRequest("testGame");
        gameService.newGame(registerResult.authToken(), request);
        ListResult result = gameService.listGames(registerResult.authToken());
        assertNotNull(result);
        assertEquals("testGame", result.getArray().getFirst().gameName());
    }

    @Test
    void negativeListGames() {
        Game gameService = new Game(aDAO, gDAO);
        assertThrows(InvalidAuthorizationException.class, () -> gameService.listGames("badAuthToken"));
    }

    @Test
    void positiveJoinResult() throws InvalidAuthorizationException, AlreadyTakenException, BadRequestResponse {
        var registerResult = register();
        Game gameService = new Game(aDAO, gDAO);
        CreateRequest request = new CreateRequest("testGame");
        gameService.newGame(registerResult.authToken(), request);

        JoinRequest joinReq = new JoinRequest("WHITE", 1);
        gameService.joinGame(registerResult.authToken(), joinReq);
        var game = gDAO.getGame(joinReq.gameID());
        assertEquals("TestUser", game.whiteUsername());
    }

    @Test
    void negativeJoinResult() throws InvalidAuthorizationException, AlreadyTakenException {
        var registerResult = register();
        Game gameService = new Game(aDAO, gDAO);
        CreateRequest request = new CreateRequest("testGame");
        gameService.newGame(registerResult.authToken(), request);
        gameService.listGames(registerResult.authToken());
        JoinRequest joinReq = new JoinRequest("white", 1);
        assertThrows(BadRequestException.class, () -> gameService.joinGame(registerResult.authToken(), joinReq));
    }

    @Test
    void positiveClear() throws InvalidAuthorizationException, AlreadyTakenException {
        positiveJoinResult();
        DeleteDB dataService = new DeleteDB(uDAO, aDAO, gDAO);
        dataService.clear();
        assertEquals(0, aDAO.getSize());
        assertEquals(0, uDAO.getSize());
        assertEquals(0, gDAO.getSize());
    }
}
