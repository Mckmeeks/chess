package dataaccess.interfaces;

import dataaccess.exceptions.DataAccessException;
import dataaccess.exceptions.InvalidAuthorizationException;
import model.AuthData;

public interface AuthDAO {
    boolean createAuth(AuthData a) throws DataAccessException;
    AuthData getAuth(String token) throws DataAccessException;
    void deleteAuth(String token) throws InvalidAuthorizationException, DataAccessException;
    void clear() throws DataAccessException;
    int getSize() throws DataAccessException;
}