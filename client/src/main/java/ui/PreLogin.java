package ui;

import exception.ResponseException;

import server.ServerFacade;

import result.*;
import request.*;

import java.util.Arrays;
import java.util.Scanner;

import static ui.EscapeSequences.*;

public class PreLogin {
    private final ServerFacade server;
    private String responseItem;

    public PreLogin(String serverURL) {
        server = new ServerFacade(serverURL);
        responseItem = null;
    }

    public void run() {
        System.out.println(WHITE_KING + " Welcome to chess! Sign in to start " +  WHITE_QUEEN);
        help();

        Scanner scanner = new Scanner(System.in);
        var userPrompt = "";
        while (!userPrompt.equals("quit")) {
            printPrompt();
            userPrompt = scanner.nextLine();

            executeCommand(userPrompt.split(" "));

            if (responseItem != null) {
                if (responseItem.equals("quit")) {userPrompt = responseItem;}
                else {
                    help();
                    responseItem = null;
                }
            }
        }
    }

    private void executeCommand(String[] prompt) {
        try {
            switch (prompt[0]) {
                case "quit" -> quit();
                case "login" -> login(prompt);
                case "register" -> register(prompt);
                default -> help();
            }
        } catch (ResponseException ex) {
            if (ex.code().equals(ResponseException.Code.ServerError)) {System.out.print("Server Error, try again");}
            else {System.out.print(Arrays.stream(ex.getMessage().split(": ")).toList().getLast());}
        } catch (Exception ex) {
            System.out.print(ex.getMessage() + "\n");
        }
    }

    private void help() {
        System.out.print(SET_TEXT_COLOR_BLUE +
        """
        
            register <USERNAME> <PASSWORD> <EMAIL> -  to create an account
            login <USERNAME> <PASSWORD> - to sign in
            quit - to exit the program
            help - to list the available commands
        """
        );
    }

    private void quit() {
        System.out.println("\nHave a great day!");
    }

    private void login(String[] prompt) throws ResponseException {
        if (prompt.length != 3) {throw new IllegalArgumentException("Invalid arguments: login requires a USERNAME and a PASSWORD");}
        LoginResult result = this.server.login(new LoginRequest(prompt[1], prompt[2]));
        PostLogin verified = new PostLogin(server, result.username(), result.authToken());
        responseItem = verified.run();
    }

    private void register(String[] prompt) throws ResponseException {
        String email;
        if (prompt.length == 4) {email = prompt[3];}
        else if (prompt.length == 3) {email = null;}
        else {throw new IllegalArgumentException("Invalid arguments: register requires a USERNAME, PASSWORD, and EMAIL");}
        RegisterResult result = this.server.register(new RegisterRequest(prompt[1], prompt[2], email));
        PostLogin verified = new PostLogin(server, prompt[1], result.authToken());
        responseItem = verified.run();
    }

    private void printPrompt() {
        System.out.print("\n" + RESET_TEXT_COLOR + "[LOGGED_OUT] >>> " + SET_TEXT_COLOR_BLUE);
    }
}
