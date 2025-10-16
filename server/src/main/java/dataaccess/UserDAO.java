package dataaccess;

import model.UserData;

public interface UserDAO {
    void createUser(UserData u) throws AlreadyTakenException;
    UserData getUser(String username);
}