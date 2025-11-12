package server;

import com.google.gson.Gson;

import model.*;

import java.net.*;
import java.net.http.*;

import exception.ResponseException;

import result.*;
import request.*;

public class ServerFacade {
    private final HttpClient client = HttpClient.newHttpClient();
    private final String serverUrl;

    public ServerFacade(String url) {
        serverUrl = url;
    }

    public RegisterResult register(RegisterRequest reg) throws ResponseException {
        HttpRequest request = buildRequest("POST", "/user", reg, null);
        HttpResponse<String> response = sendRequest(request);
        return handleResponse(response, RegisterResult.class);
    }

    public LoginResult login(LoginRequest user) throws ResponseException {
        HttpRequest request = buildRequest("POST", "/session", user, null);
        HttpResponse<String> response = sendRequest(request);
        return handleResponse(response, LoginResult.class);
    }

    public LogoutResult logout(String auth) throws ResponseException {
        HttpRequest request = buildRequest("DELETE", "/session", null, auth);
        HttpResponse<String> response = sendRequest(request);
        return handleResponse(response, LogoutResult.class);
    }

    public NewGameResult createGame(CreateRequest game, String auth) throws ResponseException {
        HttpRequest request = buildRequest("POST", "/game", game, auth);
        HttpResponse<String> response = sendRequest(request);
        return handleResponse(response, NewGameResult.class);
    }

    public ListResult listGames(String auth) throws ResponseException {
        HttpRequest request = buildRequest("GET", "/game", null, auth);
        HttpResponse<String> response = sendRequest(request);
        return handleResponse(response, ListResult.class);
    }

    public GetResult getGame(GetRequest gameID, String auth) throws ResponseException {
        HttpRequest request = buildRequest("GET", "/game/specific", gameID, auth);
        HttpResponse<String> response = sendRequest(request);
        return handleResponse(response, GetResult.class);
    }

    public JoinResult joinGame(JoinRequest join, String auth) throws ResponseException {
        HttpRequest request = buildRequest("PUT", "/game", join, auth);
        HttpResponse<String> response = sendRequest(request);
        return handleResponse(response, JoinResult.class);
    }

    public void clear() throws ResponseException {
        HttpRequest request = buildRequest("DELETE", "/db", null, null);
        sendRequest(request);
    }

    private HttpRequest buildRequest(String method, String path, Object body, String header) {
        HttpRequest.Builder request = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + path))
                .method(method, makeRequestBody(body));
        if (body != null) {
            request.setHeader("Content-Type", "application/json");
        }
        if (header != null) {
            request.setHeader("Authorization", header);
        }
        return request.build();
    }

    private HttpRequest.BodyPublisher makeRequestBody(Object body) {
//        if (body instanceof String) { return HttpRequest.BodyPublishers.ofString((String)body); }
        if (body != null) { return HttpRequest.BodyPublishers.ofString(new Gson().toJson(body)); }
        else { return HttpRequest.BodyPublishers.noBody(); }
    }

    private HttpResponse<String> sendRequest(HttpRequest request) throws ResponseException {
        try {
            return client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception ex) {
            throw new ResponseException(ResponseException.Code.ServerError, ex.getMessage());
        }
    }

    private <T> T handleResponse(HttpResponse<String> response, Class<T> responseClass) throws ResponseException {
        int status = response.statusCode();
        if (!isSuccessful(status)) {
            String body = response.body();
            if (body != null) {
                throw ResponseException.fromJson(status, body);
            }
            throw new ResponseException(ResponseException.fromHttpStatusCode(status), "other failure: " + status);
        }
        if (responseClass != null) {
            return new Gson().fromJson(response.body(), responseClass);
        }
        return null;
    }

    private boolean isSuccessful(int status) {
        return status == 200;
    }
}

// incorrect number of arguments, wrong types of arguments (a word when the code expects
//        a number, arguments in the wrong order, etc.), and arguments that the server rejects
//        (register with an existing username, login with incorrect username/password, etc.)