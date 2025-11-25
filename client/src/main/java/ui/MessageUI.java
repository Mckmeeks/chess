package ui;

import chess.ChessBoard;
import chess.ChessGame;
import model.GameData;
import websocket.messages.*;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static ui.EscapeSequences.*;
import static ui.EscapeSequences.BLACK_BISHOP;
import static ui.EscapeSequences.BLACK_KING;
import static ui.EscapeSequences.BLACK_KNIGHT;
import static ui.EscapeSequences.BLACK_PAWN;
import static ui.EscapeSequences.BLACK_QUEEN;
import static ui.EscapeSequences.BLACK_ROOK;
import static ui.EscapeSequences.EMPTY;
import static ui.EscapeSequences.RESET_BG_COLOR;
import static ui.EscapeSequences.SET_BG_COLOR_BLACK;
import static ui.EscapeSequences.SET_BG_COLOR_WHITE;
import static ui.EscapeSequences.SET_TEXT_COLOR_BLUE;
import static ui.EscapeSequences.SET_TEXT_COLOR_LIGHT_GREY;
import static ui.EscapeSequences.SET_TEXT_COLOR_MAGENTA;
import static ui.EscapeSequences.WHITE_BISHOP;
import static ui.EscapeSequences.WHITE_KING;
import static ui.EscapeSequences.WHITE_KNIGHT;
import static ui.EscapeSequences.WHITE_PAWN;
import static ui.EscapeSequences.WHITE_QUEEN;
import static ui.EscapeSequences.WHITE_ROOK;

public class MessageUI {
    PrintStream sys;
    GameData game;
    ChessGame.TeamColor color;

    public MessageUI(ChessGame.TeamColor color, GameData game) {
        sys = System.out;
        this.color = color;
        this.game = game;
    }

    public void error(ErrorMessage error) {
        sys.println(error.getErrorMessage());
    }

    public void update(LoadGame game) {
    }

    public void tell(Notification note) {
        sys.println(note.getNotification());
    }

    public void ignore() {}

    public void draw() {}

    private void printGame(GameData game) {
        var builder = new StringBuilder();
        ChessBoard board = game.game().getBoard();
        String boarderColor = SET_BG_COLOR_LIGHT_GREY + SET_TEXT_COLOR_BLUE;

        List<String> boardView = new ArrayList<>(Arrays.stream(board.toString().split("\\|")).toList());
        String backgroundColor1;
        String backgroundColor2;
        String horizontal;
        List<String> vertical;
        builder.append("\n");
        if (color.equals(ChessGame.TeamColor.BLACK)) {
            horizontal = boarderColor + "    h  g  f  e  d  c  b  a    " + RESET_BG_COLOR;
            vertical = new ArrayList<>(Arrays.stream("| 8 | 7 | 6 | 5 | 4 | 3 | 2 | 1 ".split("\\|")).toList());
            boardView = boardView.reversed();
            backgroundColor1 = SET_BG_COLOR_WHITE;
            backgroundColor2 = SET_BG_COLOR_BLACK;
        } else {
            horizontal = boarderColor + "    a  b  c  d  e  f  g  h    " + RESET_BG_COLOR;
            vertical = new ArrayList<>(Arrays.stream("| 1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 ".split("\\|")).toList());
            backgroundColor1 = SET_BG_COLOR_BLACK;
            backgroundColor2 = SET_BG_COLOR_WHITE;
        }

        boardView.add("\n");
        builder.append(horizontal);
        builder.append("\n");
        builder.append(boarderColor);
        builder.append(vertical.getLast());

        for (String part : boardView) { //This iterates through the board
            if (part.equals("\n")) {
                builder.append(boarderColor);
                builder.append(vertical.removeLast());
                builder.append(RESET_BG_COLOR);
                builder.append(part); //This is where it actually adds the piece
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
        builder.append("\n");
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
