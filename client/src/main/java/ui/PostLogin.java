package ui;

import server.ServerFacade;

public class PostLogin {
    private final ServerFacade server;
    private final String serverURL;
    private final String authToken;

    public PostLogin(String serverURL, String authToken) {
        server = new ServerFacade(serverURL);
        this.serverURL = serverURL;
        this.authToken = authToken;
    }

    public void run() {}
}
