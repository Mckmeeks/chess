package websocket;

import dataaccess.exceptions.DataAccessException;
import dataaccess.interfaces.*;

import model.AuthData;

import websocket.commands.UserGameCommand;

import io.javalin.websocket.*;

import org.jetbrains.annotations.NotNull;

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
    public void handleConnect(@NotNull WsConnectContext ctx) {
//        if (verify(ctx.header("Authorization")) == null) {
//
//        }
        System.out.printf("Websocket connected by %s", ctx.session);
        ctx.enableAutomaticPings();
    }

    @Override
    public void handleMessage(@NotNull WsMessageContext ctx) {

    }

    @Override
    public void handleClose(@NotNull WsCloseContext ctx) {

    }

    private AuthData verify(String authToken) throws DataAccessException {
        return aDAO.getAuth(authToken);
    }
}
