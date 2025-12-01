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

import static chess.ChessGame.TeamColor.*;
import static websocket.messages.Notification.NotificationType.*;


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
        System.out.printf("\nWebsocket connected by %s", ctx.sessionId());
        System.out.println("\n" + ctx.headerMap());
        System.out.println(ctx.session.getInputBufferSize());
        ctx.enableAutomaticPings();
    }

    @Override
    public void handleMessage(@NotNull WsMessageContext ctx) throws DataAccessException, IOException {
        System.out.println("Handling message");
        System.out.println("Message: " + ctx.message());
        UserGameCommand command = new Gson().fromJson(ctx.message(), UserGameCommand.class);
        if (verify(command.getAuthToken()) == null) {
            ctx.send(toJSON(new ErrorMessage("Error: unauthorized")));
            ctx.closeSession();
            System.out.printf("\n%s provided invalid authentication, dropped websocket connection", ctx.sessionId());
        } else {
            System.out.println(command.getCommandType());
            switch (command.getCommandType()) {
                case UserGameCommand.CommandType.CONNECT -> connect(command, ctx);
                case UserGameCommand.CommandType.LEAVE -> leave(command, ctx);
                case UserGameCommand.CommandType.RESIGN -> resign(command, ctx);
                case UserGameCommand.CommandType.MAKE_MOVE -> makeMove(ctx);
                default -> {}
            }
        }
    }

    @Override
    public void handleClose(@NotNull WsCloseContext ctx) {
        System.out.printf("\n%s websocket closed", ctx.sessionId());
        System.out.println("\n" + ctx.reason());
        System.out.println(ctx.sessionAttributeMap());
    }

    private void connect(UserGameCommand command, WsMessageContext ctx) throws DataAccessException, IOException {
        GameData game = gDAO.getGame(command.getGameID());
        System.out.println("Started connect method");
        if (game == null) {ctx.send(toJSON(new ErrorMessage("Error: Invalid GameID")));}
        else {
            ctx.send(new Gson().toJson(new LoadGame(game)));
            connections.add(command.getGameID(), ctx.session);
            String user = getUser(command.getAuthToken());
            connections.broadcast(command.getGameID(), ctx.session, new Notification(connectMessage(user, game), SHALOM));
            String state = stateMessage(game);
            if (!state.isEmpty()) {
                connections.broadcast(command.getGameID(), null, new Notification(state, SHALOM));
            }
        }
    }

    private void leave(UserGameCommand command, WsMessageContext ctx) throws DataAccessException, IOException {
        GameData game = gDAO.getGame(command.getGameID());
        if (game == null) {ctx.send(toJSON(new ErrorMessage("Error: Invalid GameID")));}
        else {
            String user = getUser(command.getAuthToken());
            connections.remove(game.gameID(), ctx.session);
            leaveGame(game, user);
            connections.broadcast(command.getGameID(), null, new Notification(leaveMessage(user, game), SHALOM));
            ctx.closeSession();
        }
    }

    private void resign(UserGameCommand command, WsMessageContext ctx) throws DataAccessException, IOException {
        GameData game = gDAO.getGame(command.getGameID());
        if (game == null) {ctx.send(toJSON(new ErrorMessage("Error: Invalid GameID")));}
        else {
            String user = getUser(command.getAuthToken());
            if (user == null) {ctx.send(toJSON(new ErrorMessage("Error: Invalid Authorization")));}
            else {
                ChessGame internalGame = game.game();
                if (!user.equals(game.whiteUsername()) & !user.equals(game.blackUsername()) | internalGame.getTeamTurn().equals(FINISHED)) {
                    ctx.send(toJSON(new ErrorMessage("Error: Invalid Authorization")));
                }
                else {
                    internalGame.setTeamTurn(FINISHED);
                    gDAO.updateGame(command.getGameID(), game);
                    connections.broadcast(command.getGameID(), null, new Notification(prepMessage(user, game) + " resigned", SHALOM));
                }
            }
        }
    }

    private void makeMove(WsMessageContext ctx) throws DataAccessException, IOException {
        MakeMoveCommand command = new Gson().fromJson(ctx.message(), MakeMoveCommand.class);
        GameData game = gDAO.getGame(command.getGameID());
        ChessMove move = command.getMove();
        String user = getUser(command.getAuthToken());
        if (game == null) {ctx.send(toJSON(new ErrorMessage("Error: Invalid GameID")));}
        else if (game.game().getTeamTurn() == FINISHED) {ctx.send(toJSON(new ErrorMessage("Error: game is finished")));}
        else if (move == null) {ctx.send(toJSON(new ErrorMessage("Error: Invalid Chess Move")));}
        else {
            GameData validGame = checkGameMove(game, move, user);
            ChessGame.TeamColor nextTurn = game.game().getTeamTurn();
            if (validGame == null) {
                String error = "Error: Invalid chess move";
                String status = getGameStatus(game.game(), nextTurn);
                if (!status.isEmpty()) {error += ", you are in " + status;}
                ctx.send(toJSON(new ErrorMessage(error)));
            } else {
                updateGameMove(validGame);
                connections.broadcast(command.getGameID(), null, new LoadGame(validGame));
                connections.broadcast(command.getGameID(), ctx.session, new Notification(moveMessage(user, move, validGame), MOVE));
                String state = stateMessage(validGame);
                if (!state.isEmpty()) {
                    connections.broadcast(command.getGameID(), null, new Notification(state, SHALOM));
                }
            }
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

    private String getGameStatus(ChessGame game, ChessGame.TeamColor color) {
        if (game.isInCheck(color)) {
            if (game.isInCheckmate(color)) {
                return "checkmate";
            }
            else {
                return "check";
            }
        } else if (game.isInStalemate(color)) {
            return "stalemate";
        }
        return "";
    }


    private String connectMessage(String user, GameData game) {
        return prepMessage(user, game) + " joined the game!";
    }

    private String leaveMessage(String user, GameData game) {
        return prepMessage(user, game) + " left the game :(";
    }

    private String moveMessage(String user, ChessMove move, GameData game) {
        return prepMessage(user, game) + ": " + move;
    }

    private String stateMessage(GameData validGame) {
        String opUser = "White player ";
        String gameStatus = getGameStatus(validGame.game(), WHITE);
        if (!gameStatus.isEmpty()) {
            if (validGame.whiteUsername() != null) {
                opUser = validGame.whiteUsername();
                return prepMessage(opUser, validGame) + " is in " + gameStatus;
            }
            return opUser + "is in " + gameStatus;
        } else {
            opUser = "Black player ";
            gameStatus = getGameStatus(validGame.game(), BLACK);
            if (!gameStatus.isEmpty()) {
                if (validGame.blackUsername() != null) {
                    opUser = validGame.blackUsername();
                    return prepMessage(opUser, validGame) + " is in " + gameStatus;
                }
                return opUser + "is in " + gameStatus;
            }
            return "";
        }
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
                nextColor = WHITE;
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
