package handler;

import dataaccess.InvalidAuthorizationException;

import dataaccess.interfaces.AuthDAO;
import dataaccess.interfaces.GameDAO;

import service.Game;

import service.result.NewGameResult;

public class Create extends Handler {
    private final GameDAO gDAO;

    public Create(AuthDAO authDataAcc, GameDAO gameDataAcc) {
        super(authDataAcc);
        gDAO = gameDataAcc;
    }

    public String run(String authToken, String gameName) throws InvalidAuthorizationException {
        var gameService = new Game(aDAO, gDAO);
        NewGameResult result = gameService.newGame(authToken, gameName);
        return serializer.toJson(result);
    }
}
