package server;

import dataaccess.*;
import io.javalin.*;

import dataaccess.exceptions.*;

import com.google.gson.JsonSyntaxException;

import dataaccess.interfaces.*;

import handler.*;

import service.DeleteDB;
import websocket.WebSocketHandler;

public class Server {

    private final Javalin javalin;
    private WebSocketHandler wsHandler;

    private UserDAO uDAO;
    private AuthDAO aDAO;
    private GameDAO gDAO;

    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"));
        mySqlImplementation();

        // Register your endpoints and exception handlers here.
        javalin.post("/user", context -> {
            var registrationHandler = new Registration(uDAO, aDAO);
            String result = registrationHandler.run(context.body());
            context.result(result);
        });

        javalin.post("/session", context -> {
            var loginHandler = new Login(uDAO, aDAO);
            String result = loginHandler.run(context.body());
            context.result(result);
        });

        javalin.delete("/session", context -> {
            var logoutHandler = new Logout(aDAO);
            String result = logoutHandler.run(context.header("Authorization"));
            context.result(result);
        });

        javalin.get("/game", context -> {
            var listHandler = new List(aDAO, gDAO);
            String result = listHandler.run(context.header("Authorization"));
            context.result(result);
        });

        javalin.get("/game/specific", context -> {
            var getHandler = new Get(aDAO, gDAO);
            String result = getHandler.run(context.header("Authorization"), context.body());
            context.result(result);
        });

        javalin.post("/game", context -> {
            var createHandler = new Create(aDAO, gDAO);
            String result = createHandler.run(context.header("Authorization"), context.body());
            context.result(result);
        });

        javalin.put("/game", context -> {
            var joinHandler = new Join(aDAO, gDAO);
            String result = joinHandler.run(context.header("Authorization"), context.body());
            context.result(result);
        });

        javalin.delete("/db", context -> {
            DeleteDB dataService = new DeleteDB(uDAO, aDAO, gDAO);
            dataService.clear();
            context.result("{}");
        });

        javalin.ws("/ws", ws -> {
            ws.onConnect(wsHandler);
            ws.onMessage(wsHandler);
            ws.onClose(wsHandler);
        });


        javalin.exception(JsonSyntaxException.class, (e, context) -> {
            context.status(400);
            context.result("{\"message\": \"Error: bad request\"}");
        });

        javalin.exception(BadRequestException.class, (e, context) -> {
            context.status(400);
            context.result("{\"message\": \"Error: bad request\"}");
        });

        javalin.exception(InvalidAuthorizationException.class, (e, context) -> {
            context.status(401);
            context.result("{\"message\": \"Error: unauthorized\"}");
        });

        javalin.exception(AlreadyTakenException.class, (e, context) -> {
            context.status(403);
            context.result("{\"message\": \"Error: username already taken\"}");
        });

        javalin.exception(DataAccessException.class, (e, context) -> {
            context.status(500);
            context.result("{\"message\": \"Error: (" + e.getMessage().replace("\"", "") + ")\"}");
            System.out.println(e.getMessage());
        });

        javalin.exception(Exception.class, (e, context) -> {
            context.status(500);
            context.result("{\"message\": \"Error: (" + e.getMessage().replace("\"", "") + ")\"}");
            System.out.println(e.getMessage());
        });
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }

    private void mySqlImplementation() {
        try {
            dataaccess.MySqlDAO overDAO = new MySqlDAO();
            uDAO = overDAO.getUserDAO();
            aDAO = overDAO.getAuthDAO();
            gDAO = overDAO.getGameDAO();
            wsHandler = new WebSocketHandler(aDAO, gDAO);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }
}
