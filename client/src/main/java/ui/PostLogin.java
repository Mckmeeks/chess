package ui;

import exception.ResponseException;

import model.GameData;
import chess.ChessBoard;

import request.CreateRequest;
import request.GetRequest;
import request.JoinRequest;

import result.GetResult;
import result.ListResult;
import result.NewGameResult;

import server.ServerFacade;

import java.util.*;
import java.util.function.Function;

import static ui.EscapeSequences.*;

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
        this.clientToGameIDs =  new Hashtable<>();
        this.gameToClientIDs = new Hashtable<>();
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
                case "observe" -> observeGame(prompt);
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

        String format = "\t%d.  %-13s  White: %-10s  Black: %-10s\n";
        Function<String, String> toNone = name -> {if (name == null) {return "None";} else {return name;}};
        for (int i = 1; i <= result.getArray().size(); i++) {
            GameData game = result.getArray().get(i-1);
            makeNewIdPair(i, game.gameID());
            System.out.printf(format, i, game.gameName(), toNone.apply(game.whiteUsername()), toNone.apply(game.blackUsername()));
        }
    }

    private void playGame(String[] prompt) throws ResponseException {
        if (prompt.length != 3) {throw new IllegalArgumentException("Invalid arguments: join requires a game ID and a player COLOR");}
        int id = testIdInput(prompt[1]);
        if (!prompt[2].equals("WHITE") & !prompt[2].equals("BLACK")) {
            throw new IllegalArgumentException("Invalid arguments: join requires a WHITE or BLACK color description");
        }
        server.joinGame(new JoinRequest(prompt[2], clientToGameIDs.get(id)), authToken);
        GetResult result = server.getGame(new GetRequest(clientToGameIDs.get(id)), authToken);
        printGame(result);
    }

    private void observeGame(String[] prompt) throws ResponseException {
        if (prompt.length != 2) {throw new IllegalArgumentException("Invalid arguments: observe requires a game ID");}
        int id = testIdInput(prompt[1]);
        GetResult result = server.getGame(new GetRequest(clientToGameIDs.get(id)), authToken);
        printGame(result);
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

    private void printGame(GetResult gameResult) {
        var builder = new StringBuilder();
        GameData game = gameResult.game();
        ChessBoard board = game.game().getBoard();
        String boarderColor = SET_BG_COLOR_LIGHT_GREY + SET_TEXT_COLOR_BLUE;

        List<String> boardView = new ArrayList<>(Arrays.stream(board.toString().split("\\|")).toList());
        String backgroundColor1;
        String backgroundColor2;
        String horizontal;
        List<String> vertical;
        builder.append("\n");
        if (!user.equals(game.blackUsername())) {
            horizontal = boarderColor + "    a  b  c  d  e  f  g  h    " + RESET_BG_COLOR;
            vertical = new ArrayList<>(Arrays.stream("| 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 ".split("\\|")).toList());
            boardView.removeLast();
            boardView = boardView.reversed();
            boardView.add("\n");
            builder.append(horizontal);
            builder.append("\n");
            backgroundColor1 = SET_BG_COLOR_WHITE;
            backgroundColor2 = SET_BG_COLOR_BLACK;
        } else {
            horizontal = boarderColor + "    h  g  f  e  d  c  b  a    " + RESET_BG_COLOR + "\n";
            vertical = new ArrayList<>(Arrays.stream("| 8 | 7 | 6 | 5 | 4 | 3 | 2 | 1 ".split("\\|")).toList());
            backgroundColor1 = SET_BG_COLOR_BLACK;
            backgroundColor2 = SET_BG_COLOR_WHITE;
            builder.append(horizontal);
        }
        builder.append(boarderColor);
        builder.append(vertical.getLast());

        for (String part : boardView) {
            if (part.equals("\n")) {
                builder.append(boarderColor);
                builder.append(vertical.removeLast());
                builder.append(RESET_BG_COLOR);
                builder.append(part);
                builder.append(boarderColor);
                builder.append(vertical.getLast());
            }
            builder.append(backgroundColor1);

            String tempBackground = backgroundColor2;
            backgroundColor2 = backgroundColor1;
            backgroundColor1 = tempBackground;
            builder.append(translatePiece(part));
        }

        builder.append(SET_TEXT_COLOR_BLUE);
        builder.append(RESET_BG_COLOR);
        builder.append(horizontal);
        builder.append(RESET_BG_COLOR);
        if (!user.equals(game.blackUsername())) {builder.append("\n");}
        System.out.print(builder);
    }

    private String translatePiece(String piece) {
        return switch (piece) {
            case "R" -> SET_TEXT_COLOR_MAGENTA + WHITE_ROOK;
            case "N" -> SET_TEXT_COLOR_MAGENTA + WHITE_KNIGHT;
            case "B" -> SET_TEXT_COLOR_MAGENTA + WHITE_BISHOP;
            case "Q" -> SET_TEXT_COLOR_MAGENTA + WHITE_QUEEN;
            case "K" -> SET_TEXT_COLOR_MAGENTA + WHITE_KING;
            case "P" -> SET_TEXT_COLOR_MAGENTA + WHITE_PAWN;
            case "r" -> SET_TEXT_COLOR_LIGHT_GREY + BLACK_ROOK;
            case "n" -> SET_TEXT_COLOR_LIGHT_GREY + BLACK_KNIGHT;
            case "b" -> SET_TEXT_COLOR_LIGHT_GREY + BLACK_BISHOP;
            case "q" -> SET_TEXT_COLOR_LIGHT_GREY + BLACK_QUEEN;
            case "k" -> SET_TEXT_COLOR_LIGHT_GREY + BLACK_KING;
            case "p" -> SET_TEXT_COLOR_LIGHT_GREY + BLACK_PAWN;
            case " " -> EMPTY;
            case "\n" -> "";
            default -> piece;
        };
    }
}
