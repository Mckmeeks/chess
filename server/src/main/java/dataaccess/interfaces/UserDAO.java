package dataaccess.interfaces;

import dataaccess.exceptions.AlreadyTakenException;
import dataaccess.exceptions.DataAccessException;
import model.UserData;

public interface UserDAO {
    void createUser(UserData u) throws AlreadyTakenException, DataAccessException;
    UserData getUser(String username) throws DataAccessException;
    void clear() throws DataAccessException;
    int getSize() throws DataAccessException;
}