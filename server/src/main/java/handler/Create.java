package handler;

import dataaccess.BadRequestException;
import dataaccess.DataAccessException;
import dataaccess.InvalidAuthorizationException;

import dataaccess.interfaces.AuthDAO;
import dataaccess.interfaces.GameDAO;

import handler.request.CreateRequest;
import service.Game;

import service.result.NewGameResult;

public class Create extends Handler {
    private final GameDAO gDAO;

    public Create(AuthDAO authDataAcc, GameDAO gameDataAcc) {
        super(authDataAcc);
        gDAO = gameDataAcc;
    }

    public String run(String authToken, String gameName) throws InvalidAuthorizationException, BadRequestException, DataAccessException {
        var gameService = new Game(aDAO, gDAO);
        CreateRequest request = serializer.fromJson(gameName, CreateRequest.class);
        checkArguments(request);
        NewGameResult result = gameService.newGame(authToken,  request);
        return serializer.toJson(result);
    }

    private void checkArguments(CreateRequest request) throws BadRequestException {
        if (request.gameName() == null) {throw new BadRequestException("Error: bad request");}
    }
}
