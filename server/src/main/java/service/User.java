package service;

import java.util.UUID;

import dataaccess.DataAccessException;
import dataaccess.InvalidAuthorizationException;
import dataaccess.interfaces.UserDAO;
import dataaccess.interfaces.AuthDAO;

import dataaccess.AlreadyTakenException;

import handler.request.RegisterRequest;
import service.result.RegisterResult;

import handler.request.LoginRequest;
import service.result.LoginResult;

import service.result.LogoutResult;

import model.UserData;
import model.AuthData;

public class User {
    final private UserDAO uDAO;
    final private AuthDAO aDAO;

    public User(UserDAO userDataAcc, AuthDAO authDataAcc) {
        uDAO = userDataAcc;
        aDAO = authDataAcc;
    }

    public RegisterResult register(RegisterRequest request) throws AlreadyTakenException, DataAccessException {
        uDAO.createUser(new UserData(request.username(), request.password(), request.email()));
        AuthData data = createAuthData(request.username());
        return new RegisterResult(data.username(), data.authToken());
    }

    public LoginResult login(LoginRequest request) throws InvalidAuthorizationException, DataAccessException {
        UserData userData = uDAO.getUser(request.username());
        if (userData == null) {throw new InvalidAuthorizationException("Error: unauthorized");}
        if (!userData.password().equals(request.password())) {throw new InvalidAuthorizationException("Error: unauthorized");}
        AuthData data = createAuthData(request.username());
        return new LoginResult(data.username(), data.authToken());
    }

    public LogoutResult logout(String request) throws InvalidAuthorizationException {
        aDAO.deleteAuth(request);
        return new LogoutResult();
    }

    private AuthData createAuthData(String username) {
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
}
