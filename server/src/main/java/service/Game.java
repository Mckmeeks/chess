package service;

import chess.ChessGame;
import com.google.gson.Gson;
import dataaccess.InvalidAuthorizationException;

import dataaccess.interfaces.AuthDAO;
import dataaccess.interfaces.GameDAO;

import handler.request.CreateRequest;
import service.result.NewGameResult;
import service.result.ListResult;

import model.GameData;

public class Game {
    private final AuthDAO aDAO;
    private final GameDAO gDAO;
    private final Gson serializer;

    public Game(AuthDAO authDataAcc, GameDAO gameDataAcc) {
        aDAO = authDataAcc;
        gDAO = gameDataAcc;
        serializer = new Gson();
    }

    public ListResult listGames(String authToken) throws InvalidAuthorizationException {
        if (aDAO.getAuth(authToken) == null) {throw new InvalidAuthorizationException("Error: unauthorized");}
        ListResult games = new ListResult();
        for (GameData g : gDAO.listGames()) {games.add(g);}
        return games;
    }

    public NewGameResult newGame(String authToken, String gameName) throws InvalidAuthorizationException {
        if (aDAO.getAuth(authToken) == null) {throw new InvalidAuthorizationException("Error: unauthorized");}
        int newID = makeNewID();
        CreateRequest request = serializer.fromJson(gameName, CreateRequest.class);
        gDAO.createGame(new GameData(newID, "", "", request.gameName(), new ChessGame()));
        return new NewGameResult(newID);
    }

    private int makeNewID() {
        return gDAO.getLastID() + 1;
    }
}
