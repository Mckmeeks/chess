package dataaccess.interfaces;

import dataaccess.DataAccessException;
import dataaccess.InvalidAuthorizationException;
import model.AuthData;

public interface AuthDAO {
    boolean createAuth(AuthData a) throws DataAccessException;
    AuthData getAuth(String token) throws DataAccessException;
    void deleteAuth(String token) throws InvalidAuthorizationException, DataAccessException;
    void clear() throws DataAccessException;
    int getSize() throws DataAccessException;
}