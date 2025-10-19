package dataaccess.interfaces;

import dataaccess.AlreadyTakenException;
import dataaccess.InvalidAuthorizationException;
import model.UserData;

public interface UserDAO {
    void createUser(UserData u) throws AlreadyTakenException;
    UserData getUser(String username);
    void clear();
}