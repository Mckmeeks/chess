package server;

import chess.ChessMove;

import ui.MessageUI;

import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;

import com.google.gson.Gson;
import exception.ResponseException;

import jakarta.websocket.*;

import java.net.URI;
import java.io.IOException;
import java.net.URISyntaxException;

public class WebSocketFacade extends Endpoint {
    public final WebSocketContainer container;
    public final Session session;
    public final MessageUI messageUI;
    private volatile boolean open = false;
    private final Object openLock = new Object();

    public WebSocketFacade(String url, MessageUI messageUI) throws URISyntaxException, DeploymentException, IOException {
        URI uri = new URI(url.replaceFirst("http", "ws") + "/ws");
        this.container = ContainerProvider.getWebSocketContainer();
        this.session = container.connectToServer(this, uri);
        this.messageUI = messageUI;

        this.session.addMessageHandler(String.class, message -> {
            try {
                messageUI.call(message);
            } catch (Exception e) {
                System.out.println("Error handling command: " + e.getMessage());
            }
        });
    }

    public void connect(String authToken, Integer gameID) throws ResponseException {
        try {
            waitForOpen();
            send(new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, gameID));
        } catch (Exception ex) {
            throw new ResponseException(ResponseException.Code.ServerError, ex.getMessage());
        }
    }

    public void resign(String authToken, Integer gameID) throws ResponseException {
        try {
            send(new UserGameCommand(UserGameCommand.CommandType.RESIGN, authToken, gameID));
        } catch (Exception ex) {
            throw new ResponseException(ResponseException.Code.ServerError, ex.getMessage());
        }
    }

    public void leave(String authToken, Integer gameID) throws ResponseException {
        try {
            send(new UserGameCommand(UserGameCommand.CommandType.LEAVE, authToken, gameID));
        } catch (Exception ex) {
            throw new ResponseException(ResponseException.Code.ServerError, ex.getMessage());
        }
    }

    public void makeMove(String authToken, Integer gameID, ChessMove move) throws ResponseException {
        try {
            waitForOpen();
            send(new MakeMoveCommand(authToken, gameID, move));
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {
        synchronized (openLock) {
            open = true;
            openLock.notifyAll();
        }
    }

    @Override
    public void onError(Session session, Throwable ex) {
        System.out.println(ex.getMessage());
    }

    @Override
    public void onClose(Session session, CloseReason closeReason) {
        if (closeReason.getCloseCode().getCode() != 1000) {
            System.out.println("CLOSED: " + closeReason);
        }
    }

    private void send(UserGameCommand command) {
        String json = new Gson().toJson(command);
        try {
            session.getBasicRemote().sendText(json);
        } catch (IOException ex) {
            System.out.println("IOException: " + ex.getMessage());
        }
    }

    private void waitForOpen() throws ResponseException {
        synchronized (openLock) {
            try {
                while (!open) openLock.wait();
            } catch (InterruptedException e) {
                throw new ResponseException(ResponseException.Code.ServerError, "Interrupted while waiting for WebSocket open");
            }
        }
    }
}

