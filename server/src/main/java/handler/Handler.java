package handler;

import com.google.gson.Gson;
import dataaccess.interfaces.AuthDAO;

public class Handler {
    protected final AuthDAO aDAO;
    protected final Gson serializer;

    public Handler(AuthDAO authDataAcc) {
        aDAO = authDataAcc;
        serializer = new Gson();
    }
}
