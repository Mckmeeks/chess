package server;

import io.javalin.*;

import com.google.gson.JsonSyntaxException;
import dataaccess.AlreadyTakenException;

import dataaccess.interfaces.*;
import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryGameDAO;
import dataaccess.MemoryUserDAO;

import handler.*;

import kotlin.NotImplementedError;

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



        javalin.exception(JsonSyntaxException.class, (e, context) -> {
            context.status(400);
            context.result("{\"message\": \"Error: bad request\"}");
        });

        javalin.exception(AlreadyTakenException.class, (e, context) -> {
            context.status(403);
            context.result("{\"message\": \"Error: username already taken\"}");
        });

        javalin.exception(Exception.class, (e, context) -> {
            context.status(500);
            context.result("{\"message\": " + "Error: " + e.getMessage() + "\"}");
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
