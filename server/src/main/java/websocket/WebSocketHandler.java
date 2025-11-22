package websocket;

import chess.ChessGame;
import com.google.gson.Gson;
import dataaccess.exceptions.DataAccessException;
import dataaccess.exceptions.InvalidAuthorizationException;
import dataaccess.interfaces.*;

import model.AuthData;

import io.javalin.websocket.*;

import model.GameData;
import org.jetbrains.annotations.NotNull;
import websocket.commands.UserGameCommand;
import websocket.messages.*;

import javax.xml.crypto.Data;
import java.io.IO;
import java.io.IOException;

public class WebSocketHandler implements WsConnectHandler, WsCloseHandler, WsMessageHandler {
    private ConnectionManager connections;
    private UserDAO uDAO;
    private AuthDAO aDAO;
    private GameDAO gDAO;

    public WebSocketHandler(UserDAO uDAO, AuthDAO aDAO, GameDAO gDAO) {
        this.connections = new ConnectionManager();
        this.uDAO = uDAO;
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
            ctx.send(new ErrorMessage("Error: unauthorized"));
            ctx.closeSession();
            System.out.printf("%s provided invalid authentication, dropped websocket connection", ctx.session);
//            throw new InvalidAuthorizationException("Error: unauthorized");
        }
        switch (command.getCommandType()) {
            case UserGameCommand.CommandType.CONNECT -> connect(command, ctx);
            case UserGameCommand.CommandType.LEAVE -> leave(command, ctx);
            case UserGameCommand.CommandType.RESIGN -> resign(command, ctx);
            case UserGameCommand.CommandType.MAKE_MOVE -> makeMove();
        }
    }

    @Override
    public void handleClose(@NotNull WsCloseContext ctx) {

    }


    private void connect(UserGameCommand command, WsMessageContext ctx) throws DataAccessException, IOException {
        GameData game = gDAO.getGame(command.getGameID());
        if (game == null) {ctx.send(new ErrorMessage("Invalid GameID"));}
        else {
            ctx.send(new Gson().toJson(new LoadGame(game)));
            connections.add(command.getGameID(), ctx.session);
            String user = getUser(command.getAuthToken());
            connections.broadcast(command.getGameID(), ctx.session, new Notification(connectMessage(user, game)));
        }
    }

    private void leave(UserGameCommand command, WsMessageContext ctx) throws DataAccessException, IOException {
        GameData game = gDAO.getGame(command.getGameID());
        if (game == null) {ctx.send(new ErrorMessage("Invalid GameID"));}
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
        if (game == null) {ctx.send(new ErrorMessage("Invalid GameID"));}
        else {
            String user = getUser(command.getAuthToken());
            ChessGame internalGame = game.game();
            internalGame.setTeamTurn(null);
            gDAO.updateGame(command.getGameID(), new GameData(game.gameID(), game.whiteUsername(), game.blackUsername(), game.gameName(), internalGame));
            connections.broadcast(command.getGameID(), null, new Notification(prepMessage(user, game) + "resigned"));
        }
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
}
