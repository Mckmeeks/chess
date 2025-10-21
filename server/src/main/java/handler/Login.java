package handler;

import com.google.gson.JsonSyntaxException;

import dataaccess.InvalidAuthorizationException;

import handler.request.LoginRequest;
import service.result.LoginResult;

import dataaccess.interfaces.UserDAO;
import dataaccess.interfaces.AuthDAO;

import service.User;

public class Login extends Handler {
    private final UserDAO uDAO;

    public Login(UserDAO userDataAcc, AuthDAO authDataAcc) {
        super(authDataAcc);
        uDAO = userDataAcc;
    }

    public String run(String jsonRequest) throws JsonSyntaxException, InvalidAuthorizationException {
        User userService = new User(uDAO, aDAO);
        LoginRequest request = serializer.fromJson(jsonRequest, LoginRequest.class);
        if (request.username() == null) {throw new JsonSyntaxException("Error: username not included");}
        if (request.password() == null) {throw new JsonSyntaxException("Error: password not included");}
        LoginResult result = userService.login(request);
        return serializer.toJson(result);
    }
}
