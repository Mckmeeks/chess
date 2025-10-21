package service;

import chess.ChessGame;
import dataaccess.AlreadyTakenException;
import dataaccess.BadRequestException;
import dataaccess.InvalidAuthorizationException;

import dataaccess.interfaces.AuthDAO;
import dataaccess.interfaces.GameDAO;

import handler.request.CreateRequest;
import handler.request.JoinRequest;
import model.AuthData;
import service.result.JoinResult;
import service.result.NewGameResult;
import service.result.ListResult;

import model.GameData;

public class Game {
    private final AuthDAO aDAO;
    private final GameDAO gDAO;

    public Game(AuthDAO authDataAcc, GameDAO gameDataAcc) {
        aDAO = authDataAcc;
        gDAO = gameDataAcc;
    }

    public ListResult listGames(String authToken) throws InvalidAuthorizationException {
        if (aDAO.getAuth(authToken) == null) {throw new InvalidAuthorizationException("Error: unauthorized");}
        ListResult games = new ListResult();
        for (GameData g : gDAO.listGames()) {games.add(g);}
        return games;
    }

    public NewGameResult newGame(String authToken, CreateRequest request) throws InvalidAuthorizationException {
        if (aDAO.getAuth(authToken) == null) {throw new InvalidAuthorizationException("Error: unauthorized");}
        int newID = makeNewID();
        if (request.gameName().isEmpty()) {throw new BadRequestException("Error: game name not provided");}
        gDAO.createGame(new GameData(newID, null, null, request.gameName(), new ChessGame()));
        return new NewGameResult(newID);
    }

    public JoinResult joinGame(String authToken, JoinRequest request) throws InvalidAuthorizationException, AlreadyTakenException {
        AuthData currentUser = aDAO.getAuth(authToken);
        if (currentUser == null) {throw new InvalidAuthorizationException("Error: unauthorized");}
        GameData requestedGame = gDAO.getGame(request.gameID());
        if (requestedGame == null) {throw new BadRequestException("Error: requested game does not exist");}
        switch (request.playerColor()) {
            case "WHITE" -> {
                if (requestedGame.whiteUsername() == null) {
                    gDAO.updateGame(request.gameID(), new GameData(request.gameID(), currentUser.username(), requestedGame.blackUsername(), requestedGame.gameName(), requestedGame.game()));
                }
                else { throw new AlreadyTakenException("Error: color already taken"); }
            }
            case "BLACK" -> {
                if (requestedGame.blackUsername() == null) {
                    gDAO.updateGame(request.gameID(), new GameData(request.gameID(), requestedGame.whiteUsername(), currentUser.username(), requestedGame.gameName(), requestedGame.game()));
                }
                else { throw new AlreadyTakenException("Error: color already taken"); }
            }
            default -> {
                throw new BadRequestException("Error: invalid arguments given");
            }
        }
        return new JoinResult();
    }

    private int makeNewID() {
        return gDAO.getLastID() + 1;
    }

//    private String getUser(String user) {
//        if (user == null) {return null;}
//        else (user.isEmpty())
//    }
}
