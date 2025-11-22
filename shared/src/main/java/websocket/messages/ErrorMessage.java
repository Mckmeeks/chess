package websocket.messages;

public class ErrorMessage extends ServerMessage {
    public final String message;
    public ErrorMessage(String message) {
        super(ServerMessageType.ERROR);
        this.message = message;
    }
}
