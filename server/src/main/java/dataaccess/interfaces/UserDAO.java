package dataaccess.interfaces;

import dataaccess.AlreadyTakenException;
import dataaccess.DataAccessException;
import dataaccess.InvalidAuthorizationException;
import model.UserData;

public interface UserDAO {
    void createUser(UserData u) throws AlreadyTakenException, DataAccessException;
    UserData getUser(String username) throws DataAccessException;
    void clear() throws DataAccessException;
    int getSize() throws DataAccessException;
}