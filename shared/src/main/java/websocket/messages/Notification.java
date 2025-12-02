package websocket.messages;

public class Notification extends ServerMessage {
    private final String message;
    private final NotificationType notificationType;

    public enum NotificationType {
        SHALOM,
        MOVE,
        RESIGN
    }

    public Notification(String message, NotificationType type) {
        super(ServerMessageType.NOTIFICATION);
        this.message = message;
        this.notificationType = type;
    }

    public String getNotification() {
        return message;
    }

    public NotificationType getType() {
        return notificationType;
    }
}
