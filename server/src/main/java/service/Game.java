package service;

import chess.ChessGame;
import com.google.gson.Gson;
import dataaccess.AlreadyTakenException;
import dataaccess.InvalidAuthorizationException;

import dataaccess.interfaces.AuthDAO;
import dataaccess.interfaces.GameDAO;

import handler.request.CreateRequest;
import io.javalin.http.BadRequestResponse;
import model.AuthData;
import service.result.JoinResult;
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

    public JoinResult joinGame(String authToken, String playerColor, int gameID) throws InvalidAuthorizationException, AlreadyTakenException {
        AuthData currentUser = aDAO.getAuth(authToken);
        if (currentUser == null) {throw new InvalidAuthorizationException("Error: unauthorized");}
        GameData requestedGame = gDAO.getGame(gameID);
        if (requestedGame == null) {throw new BadRequestResponse("Error: requested game does not exist");}
        switch (playerColor) {
            case "WHITE" -> {
                if (requestedGame.wUsername().isEmpty()) {
                    gDAO.updateGame(gameID, new GameData(gameID, currentUser.username(), requestedGame.bUsername(), requestedGame.gameName(), requestedGame.game()));
                }
                else { throw new AlreadyTakenException("Error: color already taken"); }
            }
            case "BLACK" -> {
                if (requestedGame.bUsername().isEmpty()) {
                    gDAO.updateGame(gameID, new GameData(gameID, requestedGame.wUsername(), currentUser.username(), requestedGame.gameName(), requestedGame.game()));
                }
                else { throw new AlreadyTakenException("Error: color already taken"); }
            }
        }
        return new JoinResult();
    }

    private int makeNewID() {
        return gDAO.getLastID() + 1;
    }
}
