package websocket.messages;

public class Notification extends ServerMessage {
    private final String note;
    public Notification(String message) {
        super(ServerMessageType.NOTIFICATION);
        note = message;
    }

    public String getNotification() {
        return note;
    }
}
