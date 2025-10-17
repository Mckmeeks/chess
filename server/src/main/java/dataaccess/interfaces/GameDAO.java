package dataaccess.interfaces;

import model.GameData;

public interface GameDAO {
    void createGame(GameData game);
    GameData getGame(int gameID);
    GameData[] listGames();
//    void updateGame(int gameID, String playerColor, String username) throws DataAccessException;
//    void updateGame(int gameID, ChessMove move) throws DataAccessException;
    void updateGame(int gameID, GameData game);
    void clear();
}
