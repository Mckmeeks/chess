package dataaccess;

import java.util.Hashtable;

import dataaccess.interfaces.UserDAO;
import model.UserData;

import javax.xml.crypto.Data;

public class MemoryUserDAO implements UserDAO {
    private Hashtable<String, UserData> userDB;

    public MemoryUserDAO() {
        userDB = new Hashtable<>();
    }

    @Override
    public void createUser(UserData u) throws AlreadyTakenException {
        if (userDB.containsKey(u.username())) {throw new AlreadyTakenException("Error: username already taken");}
        userDB.put(u.username(), u);
    }

    @Override
    public UserData getUser(String username) {
        return userDB.get(username);
    }

    @Override
    public void clear() throws DataAccessException {
        userDB = new Hashtable<>();
    }

    @Override
    public int getSize() {
        return userDB.size();
    }
}
