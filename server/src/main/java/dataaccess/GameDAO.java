package dataaccess;

import chess.ChessMove;
import model.GameData;

public interface GameDAO {
    void createGame(GameData game);
    GameData getGame(String gameID);
    GameData[] listGames();
    void updateGame(String gameID, String playerColor, String username) throws DataAccessException;
    void updateGame(String gameID, ChessMove move) throws DataAccessException;
}
