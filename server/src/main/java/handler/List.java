package handler;

import com.google.gson.Gson;
import dataaccess.InvalidAuthorizationException;
import dataaccess.interfaces.AuthDAO;
import dataaccess.interfaces.GameDAO;
import service.Game;
import service.result.ListResult;

public class List {
    private final AuthDAO aDAO;
    private final GameDAO gDAO;
    private final Gson serializer;

    public List(AuthDAO authDataAcc, GameDAO gameDataAcc) {
        aDAO = authDataAcc;
        gDAO = gameDataAcc;
        serializer = new Gson();
    }

    public String run(String authToken) throws InvalidAuthorizationException {
        var gameService = new Game(aDAO, gDAO);
        ListResult result = gameService.listGames(authToken);
        return serializer.toJson(result);
    }
}
