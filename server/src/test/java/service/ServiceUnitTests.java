package service;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import dataaccess.*;
import dataaccess.interfaces.*;

import model.*;
import handler.request.*;
import service.result.*;
import service.*;

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

    static void clear() {
        uDAO.clear();
        aDAO.clear();
        gDAO.clear();
    }

    @Test
    void positiveRegister() throws AlreadyTakenException{
        var request = new RegisterRequest("TestUser", "pass","");
        User userService = new User(uDAO, aDAO);
        var response = userService.register(request);
        assertEquals("TestUser", response.username());
        assertNotNull(response.authToken());
    }

    @Test
    void negativeRegisterTwice() throws AlreadyTakenException {
        clear();
        positiveRegister();
        var request = new RegisterRequest("TestUser", "letMeIn","not.your@cheese.com");
        User userService = new User(uDAO, aDAO);
        assertThrows(DataAccessException.class, () -> userService.register(request));
    }
}
