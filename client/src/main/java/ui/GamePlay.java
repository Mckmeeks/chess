package ui;

import chess.*;
import exception.ResponseException;
import model.GameData;
import request.GetRequest;
import result.GetResult;
import server.ServerFacade;
import server.WebSocketFacade;

import java.util.*;

import static ui.EscapeSequences.*;

public class GamePlay {
    private final WebSocketFacade webSocket;
    private final MessageUI messageUI;
    private final String user;
    private final String authToken;
    private final int gameID;
    private final GameData game;
    private final ChessGame.TeamColor userColor;

    public GamePlay(ServerFacade server, String user, String authToken, int gameID) throws ResponseException {
        this.user = user;
        this.authToken = authToken;
        this.gameID = gameID;

        GetResult result = server.getGame(new GetRequest(gameID), authToken);
        this.game = result.game();

        if (user.equals(game.whiteUsername())) {userColor = ChessGame.TeamColor.WHITE;}
        else if (user.equals(game.blackUsername())) {userColor = ChessGame.TeamColor.BLACK;}
        else {userColor = null;}

        this.messageUI = new MessageUI(userColor, this.game);
        this.webSocket = server.getWebSocket(this.messageUI);
        this.webSocket.connect(authToken, gameID);
    }

    public void run() {
        Scanner scanner = new Scanner(System.in);
        var userPrompt = "";

//        printPrompt();

        while (!userPrompt.equals("leave")) {
            userPrompt = scanner.nextLine();
            executeCommand(userPrompt.split(" "));
        }
    }

    public void executeCommand(String[] prompt) {
        try {
            switch (prompt[0]) {
                case "redraw" -> redraw();
                case "leave" -> leave();
                case "move" -> {if (userColor != null) makeMove(prompt); else help();}
                case "resign" -> {if (userColor != null) resign(); else help();}
                case "highlight" -> highlight(prompt);
                default -> help();
            }
        } catch (ResponseException ex) {
            if (ex.code().equals(ResponseException.Code.ServerError)) {System.out.print("Server Error, try again");}
            else {System.out.print(ex.getMessage());}
        } catch (Exception ex) {
            System.out.print(ex.getMessage());
        }
    }

    public void help() {
        if (userColor != null) {
            System.out.print(SET_TEXT_COLOR_BLUE +
                    """
                    
                        redraw - to redraw the chess board
                        leave - to leave the game
                        move <Start> <END> <PROMOTION> - to make a move
                        resign - to resign the game
                        highlight <Position> - to highlight available moves
                    """
            );
        } else {
            System.out.print(SET_TEXT_COLOR_BLUE +
                    """
                    
                        redraw - to redraw the chess board
                        leave - to leave the game
                        highlight <Position> - to highlight available moves
                    """
            );
        }
        printPrompt();
    }

    private void redraw() throws ResponseException {
        messageUI.draw();
    }

    private void leave() throws ResponseException {
        webSocket.leave(authToken, gameID);
    }

    private void makeMove(String[] prompt) throws ResponseException {
        if (prompt.length < 3) {throw new IllegalArgumentException("Invalid arguments: making a move requires a start and end position: move b2 b3");}
        ChessPiece.PieceType piece = null;
        if (prompt.length > 4) {piece = checkPiece(prompt[3]);}
        ChessMove proposedMove = new ChessMove(checkLoc(prompt[1]), checkLoc(prompt[2]), piece);

        webSocket.makeMove(authToken, gameID, proposedMove);
    }

    private void resign() throws ResponseException {
        webSocket.resign(authToken, gameID);
    }

    private void highlight(String[] prompt) {}

    private void printPrompt() {
        System.out.print("\n" + RESET_TEXT_COLOR + "game play [LOGGED_IN] >>> " + SET_TEXT_COLOR_BLUE);
    }

    private ChessPosition checkLoc(String loc) {
        if (loc.length() == 2) {
            String col = loc.toLowerCase().substring(0,1);
            String row = loc.substring(1,2);
            if ("abcdefgh".contains(col) & ("12345678".contains(row))) {
                return new ChessPosition(Integer.parseInt(row), "abcdefgh".indexOf(col)+1);
            }
        }
        return null;
    }

    private ChessPiece.PieceType checkPiece(String piece) {
        return switch (piece.toLowerCase()) {
            case "knight" -> ChessPiece.PieceType.KNIGHT;
            case "rook" -> ChessPiece.PieceType.ROOK;
            case "bishop" -> ChessPiece.PieceType.BISHOP;
            case "queen" -> ChessPiece.PieceType.QUEEN;
            default -> null;
        };
    }
}
