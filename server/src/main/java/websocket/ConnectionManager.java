package websocket;

import org.eclipse.jetty.websocket.api.Session;

import java.io.IOException;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

import websocket.messages.ServerMessage;

public class ConnectionManager {
    //Data structure to track and update sessions connected to a specific game
    private final ConcurrentHashMap<Integer, HashSet<Session>> connections = new ConcurrentHashMap<>();

    //add session
    public void add(Integer gameID, Session session) {
        connections.putIfAbsent(gameID, new HashSet<>());
        connections.get(gameID).add(session);
    }

    //remove session
    public void remove(Integer gameID, Session session) {
        if (connections.containsKey(gameID)) {
            connections.get(gameID).remove(session);
        }
    }

    //broadcast specific message
    public void broadcast(Integer gameID, Session excludeSession, ServerMessage notification) throws IOException {
        String msg = notification.toString();
        for (Session session : connections.get(gameID)) {
            if (session.isOpen() & !session.equals(excludeSession)) {
                session.getRemote().sendString(msg);
            }
        }
    }

}
