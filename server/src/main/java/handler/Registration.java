package handler;

import com.google.gson.JsonSyntaxException;
import dataaccess.AlreadyTakenException;

import handler.request.RegisterRequest;
import service.result.RegisterResult;

import dataaccess.interfaces.UserDAO;
import dataaccess.interfaces.AuthDAO;

import service.User;

public class Registration extends Handler {
    private final UserDAO uDAO;

    public Registration(UserDAO userDataAcc, AuthDAO authDataAcc) {
        super(authDataAcc);
        uDAO = userDataAcc;
    }

    public String run(String jsonRequest) throws JsonSyntaxException, AlreadyTakenException {
        User userService = new User(uDAO, aDAO);
        RegisterRequest request = serializer.fromJson(jsonRequest, RegisterRequest.class);
        if (request.username() == null) {throw new JsonSyntaxException("Error: Username not included");}
        if (request.password() == null) {throw new JsonSyntaxException("Error: Password not included");}
        RegisterResult result = userService.register(request);
        return serializer.toJson(result);
    }
}
