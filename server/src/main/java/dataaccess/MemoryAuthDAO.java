package dataaccess;

import java.util.Hashtable;

import dataaccess.exceptions.InvalidAuthorizationException;
import dataaccess.interfaces.AuthDAO;
import model.AuthData;

public class MemoryAuthDAO implements AuthDAO{
    private Hashtable<String, AuthData> authDataDB;

    public MemoryAuthDAO() {
        authDataDB = new Hashtable<>();
    }

    @Override
    public boolean createAuth(AuthData a) {
        if (authDataDB.containsKey(a.authToken())) {return true;}
        authDataDB.put(a.authToken(), a);
        return false;
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

    @Override
    public int getSize() {
        return authDataDB.size();
    }
}

