package handler;

import dataaccess.BadRequestException;
import dataaccess.DataAccessException;

import dataaccess.interfaces.AuthDAO;
import dataaccess.interfaces.GameDAO;

import request.GetRequest;
import result.GetResult;
import service.Game;

public class Get extends Handler {
    private final GameDAO gDAO;

    public Get(AuthDAO authDataAcc, GameDAO gameDataAcc) {
        super(authDataAcc);
        gDAO = gameDataAcc;
    }

    public String run(String authToken, String jsonRequest) throws BadRequestException, DataAccessException {
        var gameService = new Game(aDAO, gDAO);
        GetRequest request = serializer.fromJson(jsonRequest, GetRequest.class);
        checkArguments(request);
        GetResult result = gameService.getGame(authToken, request);
        return serializer.toJson(result);
    }

    private void checkArguments(GetRequest request) throws BadRequestException {
        if (request == null) {throw new BadRequestException("Error: bad request");}
    }
}
