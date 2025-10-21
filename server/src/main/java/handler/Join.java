package handler;

import dataaccess.AlreadyTakenException;
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

    public String run(String authToken, String jsonRequest) throws InvalidAuthorizationException, AlreadyTakenException {
        var gameService = new Game(aDAO, gDAO);
        JoinRequest request = serializer.fromJson(jsonRequest, JoinRequest.class);
        JoinResult result = gameService.joinGame(authToken, request.playerColor(), request.gameID());
        return serializer.toJson(result);
    }
}
