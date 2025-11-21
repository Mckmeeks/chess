package dataaccess.interfaces;

import dataaccess.exceptions.DataAccessException;
import model.GameData;

public interface GameDAO {

    void createGame(GameData game) throws DataAccessException;
    GameData getGame(int gameID) throws DataAccessException;
    GameData[] listGames() throws DataAccessException;
//    void updateGame(int gameID, String playerColor, String username) throws DataAccessException;
//    void updateGame(int gameID, ChessMove move) throws DataAccessException;
    void updateGame(int gameID, GameData game) throws DataAccessException;
    void clear() throws DataAccessException;
    int getLastID() throws DataAccessException;
    int getSize() throws DataAccessException;
}
