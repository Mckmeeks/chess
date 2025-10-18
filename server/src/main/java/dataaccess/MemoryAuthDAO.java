package dataaccess;

import java.util.Hashtable;

import dataaccess.interfaces.AuthDAO;
import model.AuthData;

public class MemoryAuthDAO implements AuthDAO{
    private Hashtable<String, AuthData> authDataDB;

    public MemoryAuthDAO() {
        authDataDB = new Hashtable<>();
    }

    @Override
    public boolean createAuth(AuthData a) {
        if (authDataDB.containsKey(a.authToken())) {return false;}
        authDataDB.put(a.authToken(), a);
        return true;
    }

    @Override
    public AuthData getAuth(String token) {
        return authDataDB.get(token);
    }

    @Override
    public void deleteAuth(String token) throws InvalidAuthorizationException {
        var removed = authDataDB.remove(token);
        if (removed == null) {throw new InvalidAuthorizationException("Error: unauthorized");}
    }

    @Override
    public void clear() {
        authDataDB = new Hashtable<>();
    }
}

