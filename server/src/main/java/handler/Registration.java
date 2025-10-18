package handler;

import com.google.gson.Gson;

import com.google.gson.JsonSyntaxException;
import dataaccess.AlreadyTakenException;

import handler.request.RegisterRequest;
import service.result.RegisterResult;

import dataaccess.interfaces.UserDAO;
import dataaccess.interfaces.AuthDAO;

import service.User;

public class Registration {
    private final AuthDAO aDAO;
    private final UserDAO uDAO;
    private final Gson serializer;

    public Registration(UserDAO userDataAcc, AuthDAO authDataAcc) {
        aDAO = authDataAcc;
        uDAO = userDataAcc;
        serializer = new Gson();
    }

    public String run(String jsonRequest) throws JsonSyntaxException, AlreadyTakenException {
        User userService = new User(uDAO, aDAO);
        RegisterRequest request = serializer.fromJson(jsonRequest, RegisterRequest.class);
        RegisterResult result = userService.register(request);
        return serializer.toJson(result);
    }
}
