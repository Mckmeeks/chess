package handler;

import dataaccess.DataAccessException;
import dataaccess.InvalidAuthorizationException;

import dataaccess.interfaces.AuthDAO;

import service.result.LogoutResult;

import service.User;

public class Logout extends Handler {

    public Logout(AuthDAO authDataAcc) {
        super(authDataAcc);
    }

    public String run(String authToken) throws InvalidAuthorizationException, DataAccessException {
        User userService = new User(null, aDAO);
        LogoutResult result = userService.logout(authToken);
        return serializer.toJson(result);
    }

}
