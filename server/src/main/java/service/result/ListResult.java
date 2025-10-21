package service.result;

import model.GameData;

import java.util.ArrayList;

public class ListResult {
    private ArrayList<GameData> games;
    public ListResult() {
        games = new ArrayList<>();
    }

    public void add(GameData g) {
        games.add(new GameData(g.gameID(), g.wUsername(), g.bUsername(), g.gameName(), null));
    }
}
