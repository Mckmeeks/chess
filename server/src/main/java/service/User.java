package service;

import java.util.UUID;

import dataaccess.interfaces.UserDAO;
import dataaccess.interfaces.AuthDAO;

import dataaccess.AlreadyTakenException;

import handler.request.RegisterRequest;
import service.result.RegisterResult;

import model.UserData;
import model.AuthData;

public class User {
    final private UserDAO uDAO;
    final private AuthDAO aDAO;

    public User(UserDAO userDataAcc, AuthDAO authDataAcc) {
        uDAO = userDataAcc;
        aDAO = authDataAcc;
    }

    public RegisterResult register(RegisterRequest request) throws AlreadyTakenException {
        uDAO.createUser(new UserData(request.username(), request.password(), request.email()));
        AuthData data = createAuthData(request.username());
        return new RegisterResult(data.username(), data.authToken());
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
