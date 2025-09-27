package chess;

public class UntrackedPieceException extends RuntimeException {
    public UntrackedPieceException() {}
    public UntrackedPieceException(String message) {
        super(message);
    }
}
