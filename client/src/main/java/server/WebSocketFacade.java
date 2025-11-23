package server;

import com.google.gson.Gson;
import exception.ResponseException;
import jakarta.websocket.*;
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

    public void resign() {

    }

    public void leave() {

    }

    public void makeMove() {

    }

    public void onOpen(Session session, EndpointConfig endpointConfig) {}

    private String toJSON(Object o) {
        return new Gson().toJson(o);
    }

    private ServerMessage fromJSON(String json) {
        return new Gson().fromJson(json, ServerMessage.class);
    }
}
