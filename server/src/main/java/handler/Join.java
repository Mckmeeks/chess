package handler;

import dataaccess.AlreadyTakenException;
import dataaccess.BadRequestException;
import dataaccess.InvalidAuthorizationException;

import dataaccess.interfaces.AuthDAO;
import dataaccess.interfaces.GameDAO;

import handler.request.JoinRequest;
import service.result.JoinResult;

import service.Game;

public class Join extends Handler {
    private final GameDAO gDAO;

    public Join(AuthDAO authDataAcc, GameDAO gameDataAcc) {
        super(authDataAcc);
        gDAO = gameDataAcc;
    }

    public String run(String authToken, String jsonRequest) throws InvalidAuthorizationException, AlreadyTakenException, BadRequestException {
        var gameService = new Game(aDAO, gDAO);
        JoinRequest request = serializer.fromJson(jsonRequest, JoinRequest.class);
        checkArguments(request);
        JoinResult result = gameService.joinGame(authToken, request);
        return serializer.toJson(result);
    }

    private void checkArguments(JoinRequest request) throws BadRequestException {
        if (request.playerColor() == null) {throw new BadRequestException("Error: bad request");}
    }
}
