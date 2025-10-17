package dataaccess.interfaces;

import dataaccess.AlreadyTakenException;
import dataaccess.InvalidAuthorizationException;
import model.AuthData;

public interface AuthDAO {
    void createAuth(AuthData a) throws AlreadyTakenException;
    AuthData getAuth(String token);
    void deleteAuth(String token) throws InvalidAuthorizationException;
    void clear();
}