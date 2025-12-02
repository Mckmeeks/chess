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
    private final String authToken;
    private final int gameID;
    private ChessGame.TeamColor userColor;
    private boolean both;

    public GamePlay(ServerFacade server, String user, String authToken, int gameID) throws ResponseException {
        this.authToken = authToken;
        this.gameID = gameID;
        this.both = false;

        GetResult result = server.getGame(new GetRequest(gameID), authToken);
        GameData game = result.game();

        if (game.whiteUsername() != null & game.blackUsername() != null) {
            if (user.equals(game.whiteUsername()) & user.equals(game.blackUsername())) {
                this.both = true;
            }
        }
        if (user.equals(game.whiteUsername())) {
            userColor = ChessGame.TeamColor.WHITE;
        } else if (user.equals(game.blackUsername())) {
            userColor = ChessGame.TeamColor.BLACK;
        } else {
            userColor = null;
        }

        this.messageUI = new MessageUI(userColor, game);
        this.webSocket = server.getWebSocket(this.messageUI);
        this.webSocket.connect(authToken, gameID);

        if (game.game().getTeamTurn().equals(ChessGame.TeamColor.FINISHED)) {
            userColor = null;
        }
    }

    public void run() {
        Scanner scanner = new Scanner(System.in);
        var userPrompt = "";

        while (!userPrompt.equals("leave")) {
            userPrompt = scanner.nextLine();
            executeCommand(userPrompt.split(" "));
        }
    }

    public void executeCommand(String[] prompt) {
        try {
            switch (prompt[0]) {
                case "redraw" -> redraw();
                case "leave" -> leave(prompt);
                case "move" -> { if (userColor != null) {makeMove(prompt);} else {help();} }
                case "resign" -> { if (userColor != null) {resign();} else {help();} }
                case "highlight" -> highlight(prompt);
                default -> help();
            }
        } catch (ResponseException ex) {
            if (ex.code().equals(ResponseException.Code.ServerError)) {System.out.print("Server Error, try again");}
            else {System.out.print(ex.getMessage());}
        } catch (IllegalArgumentException ex) {
            System.out.print(ex.getMessage() + "\n");
            printPrompt();
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

    private void leave(String[] prompt) throws ResponseException {
        if (prompt.length != 1) {throw new IllegalArgumentException("To leave the game, use the command 'leave'");}
        webSocket.leave(authToken, gameID);
    }

    private void makeMove(String[] prompt) throws ResponseException {
        if (prompt.length < 3) {throw new IllegalArgumentException("Making a move requires a start and end position (e.g. 'move b2 b3')");}
        ChessPiece.PieceType piece = null;
        if (prompt.length > 3) {piece = checkPiece(prompt[3]);}
        ChessMove proposedMove = new ChessMove(checkLoc(prompt[1]), checkLoc(prompt[2]), piece);
        if (messageUI.getCurrentGame().game().getTeamTurn() == ChessGame.TeamColor.FINISHED) {throw new IllegalArgumentException("Game is finished");}
        else if (!messageUI.getCurrentGame().game().getTeamTurn().equals(userColor)) {
            if (!both) {
                throw new IllegalArgumentException("It's not your turn!");
            }
        }
        webSocket.makeMove(authToken, gameID, proposedMove);
    }

    private void resign() throws ResponseException {
        webSocket.resign(authToken, gameID);
    }

    private void highlight(String[] prompt) {
        if (prompt.length != 2) {throw new IllegalArgumentException("Highlighting valid moves requires a piece to highlight (e.g. 'highlight d2')");}
        else {
            int row = 0;
            int col = 0;
            try {
                col = "abcdefgh".indexOf(prompt[1].substring(0, 1)) + 1;
                row = Integer.parseInt(prompt[1].substring(1, 2));
                if (col < 1 | row < 1 | row > 8) {
                    throw new Exception("");
                }
            } catch (Exception ex) {
                throw new IllegalArgumentException("Highlighting valid moves requires a piece to highlight (e.g 'highlight d2')");
            }
            messageUI.highlightDraw(new ChessPosition(row, col));
        }
    }

    private void printPrompt() {
        System.out.print("\n" + RESET_TEXT_COLOR + "[LOGGED_IN] >>> " + SET_TEXT_COLOR_BLUE);
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
