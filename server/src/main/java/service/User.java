package service;

import java.util.UUID;

import dataaccess.exceptions.DataAccessException;
import dataaccess.exceptions.InvalidAuthorizationException;

import dataaccess.interfaces.UserDAO;
import dataaccess.interfaces.AuthDAO;

import request.RegisterRequest;
import org.mindrot.jbcrypt.BCrypt;

import request.LoginRequest;

import result.RegisterResult;
import result.LoginResult;
import result.LogoutResult;

import model.UserData;
import model.AuthData;

public class User {
    final private UserDAO uDAO;
    final private AuthDAO aDAO;

    public User(UserDAO userDataAcc, AuthDAO authDataAcc) {
        uDAO = userDataAcc;
        aDAO = authDataAcc;
    }

    public RegisterResult register(RegisterRequest request) throws DataAccessException {
        uDAO.createUser(new UserData(request.username(), request.password(), request.email()));
        AuthData data = createAuthData(request.username());
        return new RegisterResult(data.username(), data.authToken());
    }

    public LoginResult login(LoginRequest request) throws DataAccessException {
        UserData userData = uDAO.getUser(request.username());
        if (userData == null) {throw new InvalidAuthorizationException("Error: unauthorized");}
        if (!compEncPass(request.password(), userData.password())) {throw new InvalidAuthorizationException("Error: unauthorized");}
        AuthData data = createAuthData(request.username());
        return new LoginResult(data.username(), data.authToken());
    }

    public LogoutResult logout(String request) throws DataAccessException {
        aDAO.deleteAuth(request);
        return new LogoutResult();
    }

    private AuthData createAuthData(String username) throws DataAccessException {
        boolean needed = true;
        AuthData data = null;
        while (needed) {
            data = new AuthData(generateAuthToken(), username);
            needed = aDAO.createAuth(data);
        }
        return data;
    }

    private String generateAuthToken() {
        return UUID.randomUUID().toString();
    }

    private boolean compEncPass(String plain, String enc) {
        return BCrypt.checkpw(plain, enc);
    }
}
