package websockets;

import chess.ChessGame;
import chess.ChessMove;
import chess.InvalidMoveException;

import dataaccess.exceptions.DataAccessException;
import dataaccess.interfaces.*;

import model.AuthData;
import model.GameData;

import java.io.IOException;
import io.javalin.websocket.*;

import org.jetbrains.annotations.NotNull;

import websocket.commands.*;
import websocket.messages.*;

import com.google.gson.Gson;


public class WebSocketHandler implements WsConnectHandler, WsCloseHandler, WsMessageHandler {
    private final ConnectionManager connections;
    private final AuthDAO aDAO;
    private final GameDAO gDAO;

    public WebSocketHandler(AuthDAO aDAO, GameDAO gDAO) {
        this.connections = new ConnectionManager();
        this.aDAO = aDAO;
        this.gDAO = gDAO;
    }

    @Override
    public void handleConnect(@NotNull WsConnectContext ctx){
        System.out.printf("Websocket connected by %s", ctx.session);
        ctx.enableAutomaticPings();
    }

    @Override
    public void handleMessage(@NotNull WsMessageContext ctx) throws DataAccessException, IOException {
        UserGameCommand command = new Gson().fromJson(ctx.message(), UserGameCommand.class);
        if (verify(command.getAuthToken()) == null) {
            ctx.send(toJSON(new ErrorMessage("Error: unauthorized")));
            ctx.closeSession();
            System.out.printf("\n%s provided invalid authentication, dropped websocket connection", ctx.session);
        }
        switch (command.getCommandType()) {
            case UserGameCommand.CommandType.CONNECT -> connect(command, ctx);
            case UserGameCommand.CommandType.LEAVE -> leave(command, ctx);
            case UserGameCommand.CommandType.RESIGN -> resign(command, ctx);
            case UserGameCommand.CommandType.MAKE_MOVE -> makeMove(ctx);
        }
    }

    @Override
    public void handleClose(@NotNull WsCloseContext ctx) {
        System.out.printf("\n%s websocket closed", ctx.session);
    }


    private void connect(UserGameCommand command, WsMessageContext ctx) throws DataAccessException, IOException {
        GameData game = gDAO.getGame(command.getGameID());
        if (game == null) {ctx.send(toJSON(new ErrorMessage("Error: Invalid GameID")));}
        else {
            ctx.send(new Gson().toJson(new LoadGame(game)));
            connections.add(command.getGameID(), ctx.session);
            String user = getUser(command.getAuthToken());
            connections.broadcast(command.getGameID(), ctx.session, new Notification(connectMessage(user, game)));
        }
    }

    private void leave(UserGameCommand command, WsMessageContext ctx) throws DataAccessException, IOException {
        GameData game = gDAO.getGame(command.getGameID());
        if (game == null) {ctx.send(toJSON(new ErrorMessage("Error: Invalid GameID")));}
        else {
            String user = getUser(command.getAuthToken());
            connections.remove(game.gameID(), ctx.session);
            leaveGame(game, user);
            connections.broadcast(command.getGameID(), null, new Notification(leaveMessage(user, game)));
            ctx.closeSession();
        }
    }

    private void resign(UserGameCommand command, WsMessageContext ctx) throws DataAccessException, IOException {
        GameData game = gDAO.getGame(command.getGameID());
        if (game == null) {ctx.send(toJSON(new ErrorMessage("Error: Invalid GameID")));}
        else {
            String user = getUser(command.getAuthToken());
            ChessGame internalGame = game.game();
            internalGame.setTeamTurn(null);
            gDAO.updateGame(command.getGameID(), new GameData(game.gameID(), game.whiteUsername(), game.blackUsername(), game.gameName(), internalGame));
            connections.broadcast(command.getGameID(), null, new Notification(prepMessage(user, game) + "resigned"));
        }
    }

    private void makeMove(WsMessageContext ctx) throws DataAccessException, IOException {
        MakeMoveCommand command = new Gson().fromJson(ctx.message(), MakeMoveCommand.class);
        GameData game = gDAO.getGame(command.getGameID());
        ChessMove move = command.getMove();
        String user = getUser(command.getAuthToken());
        if (game == null) {ctx.send(toJSON(new ErrorMessage("Error: Invalid GameID")));}
        else if (move == null) {ctx.send(toJSON(new ErrorMessage("Error: Invalid Chess Move")));}
        else {
            GameData validGame = checkGameMove(game, move, user);
            if (validGame == null) {ctx.send(toJSON(new ErrorMessage("Error: Invalid Chess Move")));}
            updateGameMove(validGame);
//            ctx.send(toJSON(new LoadGame(validGame)));
            connections.broadcast(command.getGameID(), null, new LoadGame(validGame));
            connections.broadcast(command.getGameID(), ctx.session, new Notification(moveMessage(user, move)));
        }
    }

    private String toJSON(Object o) {
        return new Gson().toJson(o);
    }


    private AuthData verify(String authToken) throws DataAccessException {
        return aDAO.getAuth(authToken);
    }

    private String getUser(String authToken) throws DataAccessException {
        return verify(authToken).username();
    }


    private String connectMessage(String user, GameData game) {
        return prepMessage(user, game) + " joined the game!";
    }

    private String leaveMessage(String user, GameData game) {
        return prepMessage(user, game) + " left the game :(";
    }

    private String moveMessage(String user, ChessMove move) {
        return user + ": " + move;
    }

    private String prepMessage(String user, GameData game) {
        String playerColor = "";
        if (user.equals(game.whiteUsername())) {playerColor = "White player ";}
        else if (user.equals(game.blackUsername())) {playerColor =  "Black player ";}
        return playerColor + user;
    }


    private void leaveGame(GameData game, String user) throws DataAccessException {
        String whiteUser = game.whiteUsername();
        String blackUser = game.blackUsername();
        if (user.equals(whiteUser)) {
            whiteUser = null;
        }
        else if (user.equals(blackUser)) {
            blackUser = null;
        }
        if (whiteUser == null | blackUser == null) {
            gDAO.updateGame(game.gameID(), new GameData(game.gameID(), whiteUser, blackUser, game.gameName(), game.game()));
        }
    }

    private GameData checkGameMove(GameData game, ChessMove move, String user) {
        if (move.getStartPosition() == null | move.getEndPosition() == null) {return null;}

        ChessGame chessGame = game.game();
        ChessGame.TeamColor turnColor = chessGame.getTeamTurn();
        ChessGame.TeamColor nextColor;

        switch (turnColor) {
            case WHITE -> {
                if (!user.equals(game.whiteUsername())) {
                    return null;
                }
                nextColor = ChessGame.TeamColor.BLACK;
            }
            case BLACK -> {
                if (!user.equals(game.blackUsername())) {
                    return null;
                }
                nextColor = ChessGame.TeamColor.WHITE;
            }
            default -> {return null;}
        }

        try {
            chessGame.makeMove(move);
            chessGame.setTeamTurn(nextColor);
            return new GameData(game.gameID(), game.whiteUsername(), game.blackUsername(), game.gameName(), chessGame);
        }
        catch (InvalidMoveException ex) {return null;}
    }

    private void updateGameMove(GameData game) throws DataAccessException {
        gDAO.updateGame(game.gameID(), game);
    }
}
