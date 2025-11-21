package service;

import dataaccess.exceptions.DataAccessException;
import dataaccess.interfaces.AuthDAO;
import dataaccess.interfaces.GameDAO;
import dataaccess.interfaces.UserDAO;

public class DeleteDB {
    final private UserDAO uDAO;
    final private AuthDAO aDAO;
    final private GameDAO gDAO;

    public DeleteDB(UserDAO userDataAcc, AuthDAO authDataAcc, GameDAO gameDataAcc) {
        uDAO = userDataAcc;
        aDAO = authDataAcc;
        gDAO = gameDataAcc;
    }

    public void clear(){
        try {
            uDAO.clear();
            aDAO.clear();
            gDAO.clear();
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
