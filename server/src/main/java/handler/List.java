package handler;

import dataaccess.InvalidAuthorizationException;

import dataaccess.interfaces.AuthDAO;
import dataaccess.interfaces.GameDAO;

import service.Game;

import service.result.ListResult;

public class List extends Handler {
    private final GameDAO gDAO;

    public List(AuthDAO authDataAcc, GameDAO gameDataAcc) {
        super(authDataAcc);
        gDAO = gameDataAcc;
    }

    public String run(String authToken) throws InvalidAuthorizationException {
        var gameService = new Game(aDAO, gDAO);
        ListResult result = gameService.listGames(authToken);
        return serializer.toJson(result);
    }
}
