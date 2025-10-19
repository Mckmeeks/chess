package handler;

import com.google.gson.Gson;
import dataaccess.InvalidAuthorizationException;

import dataaccess.interfaces.AuthDAO;

import service.User;
import service.result.LogoutResult;


public class Logout {
    private final AuthDAO aDAO;
    private final Gson serializer;

    public Logout(AuthDAO authDataAcc) {
        aDAO = authDataAcc;
        serializer = new Gson();
    }

    public String run(String authToken) throws InvalidAuthorizationException {
        User userService = new User(null, aDAO);
        LogoutResult result = userService.logout(authToken);
        return serializer.toJson(result);
    }

}
