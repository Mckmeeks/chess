package server;

import chess.ChessMove;
import com.google.gson.Gson;
import exception.ResponseException;
import jakarta.websocket.*;
import ui.MessageUI;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.Notification;
import websocket.messages.ServerMessage;
import websocket.messages.LoadGame;

import java.io.IOException;

import java.net.URI;
import java.net.URISyntaxException;

public class WebSocketFacade extends Endpoint {
    public Session session;

    public WebSocketFacade(String url, MessageUI messageUI) throws URISyntaxException, DeploymentException, IOException {
        URI uri = new URI(url.replaceFirst("http", "ws") + "/ws");
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        session = container.connectToServer(this, uri);
//        MessageUI messageUI = new MessageUI();

        this.session.addMessageHandler(new MessageHandler.Whole<String>() {
                public void onMessage(String message) {
                    ServerMessage mess = fromJSON(message);
                    switch (mess.getServerMessageType()) {
                        case ERROR -> {messageUI.error(new Gson().fromJson(message, ErrorMessage.class));}
                        case LOAD_GAME -> {messageUI.update(new Gson().fromJson(message, LoadGame.class));}
                        case NOTIFICATION -> {messageUI.tell(new Gson().fromJson(message, Notification.class));}
                        default -> {messageUI.ignore();}
                    }
                }
            }
        );
    }

    public void connect(String authToken, Integer gameID) throws ResponseException {
        try {
            session.getBasicRemote().sendText(toJSON(new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, gameID)));
        } catch (IOException ex) {
            throw new ResponseException(ResponseException.Code.ServerError, ex.getMessage());
        }
    }

    public void resign(String authToken, Integer gameID) throws ResponseException {
        try {
            session.getBasicRemote().sendText(toJSON(new UserGameCommand(UserGameCommand.CommandType.RESIGN, authToken, gameID)));
        } catch (IOException ex) {
            throw new ResponseException(ResponseException.Code.ServerError, ex.getMessage());
        }
    }

    public void leave(String authToken, Integer gameID) throws ResponseException {
        try {
            session.getBasicRemote().sendText(toJSON(new UserGameCommand(UserGameCommand.CommandType.LEAVE, authToken, gameID)));
        } catch (IOException ex) {
            throw new ResponseException(ResponseException.Code.ServerError, ex.getMessage());
        }
    }

    public void makeMove(String authToken, Integer gameID, ChessMove move) throws ResponseException {
        try {
            session.getBasicRemote().sendText(toJSON(new MakeMoveCommand(authToken, gameID, move)));
        } catch (IOException ex) {
            throw new ResponseException(ResponseException.Code.ServerError, ex.getMessage());
        }
    }

    public void onOpen(Session session, EndpointConfig endpointConfig) {}

    private String toJSON(Object o) {
        return new Gson().toJson(o);
    }

    private ServerMessage fromJSON(String json) {
        return new Gson().fromJson(json, ServerMessage.class);
    }
}
