package dataaccess;

import java.util.Hashtable;

import dataaccess.interfaces.GameDAO;
import model.GameData;

public class MemoryGameDAO implements GameDAO {
    private Hashtable<Integer, GameData> gameDB;
    private int lastID = 0;

    public MemoryGameDAO() {
        gameDB = new Hashtable<>();
    }

    @Override
    public void createGame(GameData game) {
        gameDB.put(game.gameID(), game);
        lastID = game.gameID();
    }

    @Override
    public GameData getGame(int gameID) {
        return gameDB.get(gameID);
    }

    @Override
    public GameData[] listGames() {
        return gameDB.values().toArray(new GameData[0]);
    }

    @Override
    public void updateGame(int gameID, GameData game) {
        gameDB.replace(gameID, game);
    }

    @Override
    public void clear() {
        gameDB = new Hashtable<>();
    }

    @Override
    public int getLastID() { return lastID;}
}
