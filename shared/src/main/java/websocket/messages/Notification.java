package websocket.messages;

public class Notification extends ServerMessage {
    private final String message;
    private final nType notificationType;

    public enum nType {
        SHALOM,
        MOVE,
    }

    public Notification(String message, nType type) {
        super(ServerMessageType.NOTIFICATION);
        this.message = message;
        this.notificationType = type;
    }

    public String getNotification() {
        return message;
    }

    public nType getType() {
        return notificationType;
    }
}
