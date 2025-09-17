package chess;

import java.util.Collection;
import java.util.Objects;
import chess.PositionCalculator.ChessPositionCalculator;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    private final ChessGame.TeamColor color;
    private final PieceType type;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.color = pieceColor;
        this.type = type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return this.color;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return this.type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        var calculator = ChessPositionCalculator.getCalculator(board, myPosition, this);
        return calculator.getMoves();
    }

    public ChessPiece copy() {
        return new ChessPiece(this.color, this.type);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || (o.getClass() != this.getClass())) return false;
        ChessPiece comp = (ChessPiece)o;
        return (this.color.equals(comp.color) && this.type.equals(comp.type));
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.color, this.type);
    }

    @Override
    public String toString() {
        String temp;
        if ((ChessPiece.PieceType.KNIGHT).equals(this.getPieceType())) {
            temp = "n";
        } else {
            temp = String.format("%s", this.type).substring(0,1);
        }
        if ((ChessGame.TeamColor.BLACK).equals(this.getTeamColor())) {
            temp = temp.toLowerCase();
        } else {
            temp = temp.toUpperCase();
        }
        return temp;
    }
}
