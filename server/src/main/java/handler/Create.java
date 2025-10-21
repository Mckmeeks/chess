package handler;

import com.google.gson.Gson;
import dataaccess.InvalidAuthorizationException;
import dataaccess.interfaces.AuthDAO;
import dataaccess.interfaces.GameDAO;
import service.Game;
import service.result.NewGameResult;

public class Create {
    private final AuthDAO aDAO;
    private final GameDAO gDAO;
    private final Gson serializer;

    public Create(AuthDAO authDataAcc, GameDAO gameDataAcc) {
        aDAO = authDataAcc;
        gDAO = gameDataAcc;
        serializer = new Gson();
    }

    public String run(String authToken, String gameName) throws InvalidAuthorizationException {
        var gameService = new Game(aDAO, gDAO);
        NewGameResult result = gameService.newGame(authToken, gameName);
        return serializer.toJson(result);
    }
}
