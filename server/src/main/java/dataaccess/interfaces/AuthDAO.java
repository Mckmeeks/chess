package dataaccess.interfaces;

import dataaccess.InvalidAuthorizationException;
import model.AuthData;

public interface AuthDAO {
    boolean createAuth(AuthData a);
    AuthData getAuth(String token);
    void deleteAuth(String token) throws InvalidAuthorizationException;
    void clear();
    int getSize();
}