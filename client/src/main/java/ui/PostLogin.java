package ui;

import exception.ResponseException;
import model.GameData;
import request.CreateRequest;
import request.JoinRequest;
import result.ListResult;
import result.NewGameResult;
import server.ServerFacade;

import java.util.Hashtable;
import java.util.Scanner;

import static ui.EscapeSequences.*;
import static ui.EscapeSequences.WHITE_QUEEN;

public class PostLogin {
    private final ServerFacade server;
    private final String user;
    private final String authToken;
    private Hashtable<Integer, Integer> clientToGameIDs;
    private Hashtable<Integer, Integer> gameToClientIDs;

    public PostLogin(ServerFacade server, String user, String authToken) {
        this.server = server;
        this.user = user;
        this.authToken = authToken;
        this.clientToGameIDs =  new Hashtable<Integer, Integer>();
        this.gameToClientIDs = new Hashtable<Integer, Integer>();
    }

    public String run() {
        System.out.println(ERASE_SCREEN + WHITE_KING + " Welcome " + user + "! Let's play some chess!" +  WHITE_QUEEN);
        help();

        Scanner scanner = new Scanner(System.in);
        var userPrompt = "";
        while (!userPrompt.equals("logout") & !userPrompt.equals("quit")) {
            printPrompt();
            userPrompt = scanner.nextLine();

            executeCommand(userPrompt.split(" "));
        }
        return userPrompt;
    }

    private void executeCommand(String[] prompt) {
        try {
            switch (prompt[0]) {
                case "logout" -> logout();
                case "create" -> createGame(prompt);
                case "list" -> listGames();
                case "join" -> playGame(prompt);
                case "observe" -> observeGame();
                case "quit" -> quit();
                default -> help();
            }
        } catch (ResponseException ex) {
            if (ex.code().equals(ResponseException.Code.ServerError)) {System.out.print("Server Error, try again");}
            else {System.out.print(ex.getMessage());}
        } catch (Exception ex) {
            System.out.print(ex.getMessage());
        }
    }

    private void help() {
        System.out.print(SET_TEXT_COLOR_BLUE +
            """
            
                create <NAME> - to create a new game
                list - to view a list of all games
                join <ID> [WHITE|BLACK] - to join a game
                observe <ID> - to observe a game
                logout - to sign out
                quit - to exit the program
                help - to to list the available commands
            """
        );
    }

    private void quit() {
        try {
            logout();
        } catch (Exception ex) {
            System.out.print("Farewell " + user + "\n");
        }
    }

    private void logout() throws ResponseException {
        server.logout(authToken);
        System.out.print("Farewell " + user + "\n");
    }

    private void createGame(String[] prompt) throws ResponseException {
        if (prompt.length != 2) {throw new IllegalArgumentException("Invalid arguments: create requires a game NAME");}
        NewGameResult result = server.createGame(new CreateRequest(prompt[1]), authToken);
        System.out.print(prompt[1] + " successfully created!");
        makeNewIdPair(clientToGameIDs.size() + 1, result.gameID());
    }

    private void listGames() throws ResponseException {
        ListResult result = server.listGames(authToken);
        wipeIdPairs();
        System.out.println();
        for (int i = 1; i <= result.getArray().size(); i++) {
            GameData tempGame = result.getArray().get(i-1);
            makeNewIdPair(i, tempGame.gameID());
            System.out.println("\t" + i + ".\t" + tempGame.gameName() + "\tWhite: " + tempGame.whiteUsername() + "\t\tBlack: " + tempGame.blackUsername());
        }
    }

    private void playGame(String[] prompt) throws ResponseException {
        if (prompt.length != 3) {throw new IllegalArgumentException("Invalid arguments: join requires a game ID and a player COLOR");}
        int id = testIdInput(prompt[1]);
        if (!prompt[2].equals("WHITE") & !prompt[2].equals("BLACK")) {throw new IllegalArgumentException("Invalid arguments: join requires a WHITE or BLACK color description");}
        server.joinGame(new JoinRequest(prompt[2], clientToGameIDs.get(id)), authToken);
    }

    private void observeGame(String[] prompt) {
        int id = testIdInput(prompt[1]);

    }

    private void printPrompt() {
        System.out.print("\n" + RESET_TEXT_COLOR + "[LOGGED_IN] >>> " + SET_TEXT_COLOR_BLUE);
    }

    private void wipeIdPairs() {
        clientToGameIDs = new Hashtable<>();
        gameToClientIDs = new Hashtable<>();
    }

    private void makeNewIdPair(Integer client, Integer game) {
        clientToGameIDs.put(client, game);
        gameToClientIDs.put(game, client);
    }

    private int testIdInput(String sd) {
        int id;
        try {id = Integer.parseInt(sd);}
        catch (Exception ex) {throw new IllegalArgumentException("Invalid game ID");}
        if (clientToGameIDs.get(id) == null) {throw new IllegalArgumentException("Invalid game ID");}
        return id;
    }
}
