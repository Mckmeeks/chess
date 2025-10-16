package model;

import chess.ChessGame;

public record GameData(int gameID, String wUsername, String bUsername, String gameName, ChessGame game) {}