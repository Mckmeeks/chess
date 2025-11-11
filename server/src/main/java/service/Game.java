package service;

import chess.ChessGame;

import dataaccess.AlreadyTakenException;
import dataaccess.BadRequestException;
import dataaccess.DataAccessException;
import dataaccess.InvalidAuthorizationException;

import dataaccess.interfaces.AuthDAO;
import dataaccess.interfaces.GameDAO;

import request.CreateRequest;
import request.JoinRequest;

import result.JoinResult;
import result.NewGameResult;
import result.ListResult;

import model.GameData;
import model.AuthData;

public class Game {
    private final AuthDAO aDAO;
    private final GameDAO gDAO;

    public Game(AuthDAO authDataAcc, GameDAO gameDataAcc) {
        aDAO = authDataAcc;
        gDAO = gameDataAcc;
    }

    public ListResult listGames(String authToken) throws DataAccessException {
        if (aDAO.getAuth(authToken) == null) {throw new InvalidAuthorizationException("Error: unauthorized");}
        ListResult games = new ListResult();
        for (GameData g : gDAO.listGames()) {games.add(g);}
        return games;
    }

    public NewGameResult newGame(String authToken, CreateRequest request) throws DataAccessException {
        if (aDAO.getAuth(authToken) == null) {throw new InvalidAuthorizationException("Error: unauthorized");}
        int newID = makeNewID();
        if (request.gameName().isEmpty()) {throw new BadRequestException("Error: game name not provided");}
        gDAO.createGame(new GameData(newID, null, null, request.gameName(), new ChessGame()));
        return new NewGameResult(newID);
    }

    public JoinResult joinGame(String authToken, JoinRequest request) throws DataAccessException {
        AuthData currentUser = aDAO.getAuth(authToken);
        if (currentUser == null) {throw new InvalidAuthorizationException("Error: unauthorized");}
        GameData reqGame = gDAO.getGame(request.gameID());
        if (reqGame == null) {throw new BadRequestException("Error: requested game does not exist");}
        switch (request.playerColor()) {
            case "WHITE" -> {
                if (reqGame.whiteUsername() == null) {
                    var tGame = new GameData(request.gameID(), currentUser.username(), reqGame.blackUsername(), reqGame.gameName(), reqGame.game());
                    gDAO.updateGame(request.gameID(), tGame);
                }
                else { throw new AlreadyTakenException("Error: color already taken"); }
            }
            case "BLACK" -> {
                if (reqGame.blackUsername() == null) {
                    var tGame = new GameData(request.gameID(), reqGame.whiteUsername(), currentUser.username(), reqGame.gameName(), reqGame.game());
                    gDAO.updateGame(request.gameID(), tGame);
                }
                else { throw new AlreadyTakenException("Error: color already taken"); }
            }
            default -> {
                throw new BadRequestException("Error: invalid arguments given");
            }
        }
        return new JoinResult();
    }

    private int makeNewID() throws DataAccessException {
        return gDAO.getLastID() + 1;
    }


}
