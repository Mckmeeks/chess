package server;

import chess.ChessMove;
import com.google.gson.Gson;
import jakarta.websocket.*;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.URISyntaxException;

public class WebSocketFacade extends Endpoint {
    public Session session;

    public WebSocketFacade(String url) throws URISyntaxException, DeploymentException, IOException {
        URI uri = new URI(url.replaceFirst("http", "ws") + "/ws");
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        session = container.connectToServer(this, uri);

        this.session.addMessageHandler(new MessageHandler.Whole<String>() {
                public void onMessage(String message) {
                    ServerMessage mess = fromJSON(message);
                    switch (mess.getServerMessageType()) {
                        case ERROR -> {}
                        case LOAD_GAME -> {}
                        case NOTIFICATION -> {}
                        default -> {}
                    }
                }
            }
        );
    }

    public void connect(String authToken, Integer gameID) throws IOException {
        session.getBasicRemote().sendText(toJSON(new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, gameID)));
    }

    public void resign(String authToken, Integer gameID) throws IOException {
        session.getBasicRemote().sendText(toJSON(new UserGameCommand(UserGameCommand.CommandType.RESIGN, authToken, gameID)));
    }

    public void leave(String authToken, Integer gameID) throws IOException {
        session.getBasicRemote().sendText(toJSON(new UserGameCommand(UserGameCommand.CommandType.LEAVE, authToken, gameID)));
    }

    public void makeMove(String authToken, Integer gameID, ChessMove move) throws IOException {
        session.getBasicRemote().sendText(toJSON(new MakeMoveCommand(authToken, gameID, move)));
    }

    public void onOpen(Session session, EndpointConfig endpointConfig) {}

    private String toJSON(Object o) {
        return new Gson().toJson(o);
    }

    private ServerMessage fromJSON(String json) {
        return new Gson().fromJson(json, ServerMessage.class);
    }
}
