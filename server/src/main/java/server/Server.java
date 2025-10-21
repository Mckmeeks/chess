package server;

import dataaccess.*;
import io.javalin.*;

import com.google.gson.JsonSyntaxException;

import dataaccess.interfaces.*;

import handler.*;

import kotlin.NotImplementedError;
import service.DeleteDB;

public class Server {

    private final Javalin javalin;
    private UserDAO uDAO;
    private AuthDAO aDAO;
    private GameDAO gDAO;

    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"));
        memoryImplementation();

        // Register your endpoints and exception handlers here.
        javalin.post("/user", context -> {
            var RegistrationHandler = new Registration(uDAO, aDAO);
            String result = RegistrationHandler.run(context.body());
            context.result(result);
        });

        javalin.post("/session", context -> {
            var LoginHandler = new Login(uDAO, aDAO);
            String result = LoginHandler.run(context.body());
            context.result(result);
        });

        javalin.delete("/session", context -> {
            var LogoutHandler = new Logout(aDAO);
            String result = LogoutHandler.run(context.header("Authorization"));
            context.result(result);
        });

        javalin.get("/game", context -> {
            var ListHandler = new List(aDAO, gDAO);
            String result = ListHandler.run(context.header("Authorization"));
            context.result(result);
        });

        javalin.post("/game", context -> {
            var CreateHandler = new Create(aDAO, gDAO);
            String result = CreateHandler.run(context.header("Authorization"), context.body());
            context.result(result);
        });

        javalin.delete("/db", context -> {
            DeleteDB dataService = new DeleteDB(uDAO, aDAO, gDAO);
            dataService.clear();
            context.result("{}");
        });


        javalin.exception(JsonSyntaxException.class, (e, context) -> {
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

    private void memoryImplementation() {
        uDAO = new MemoryUserDAO();
        aDAO = new MemoryAuthDAO();
        gDAO = new MemoryGameDAO();
    }

    private void dbImplementation() {
        throw new NotImplementedError();
    }
}
