package websocket.messages;

public class ErrorMessage extends ServerMessage {
    public final String errorMessage;

    public ErrorMessage(String message) {
        super(ServerMessageType.ERROR);
        this.errorMessage = message;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
