package chess;

import java.util.Arrays;
import java.util.Objects;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {

    public ChessPiece[][] board = new ChessPiece[8][8];

    public ChessBoard() {}
    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        this.board[position.getRow()-1][position.getColumn()-1] = piece;
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        return this.board[position.getRow()-1][position.getColumn()-1];
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        ChessPiece rook = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.ROOK);
        ChessPiece knight = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KNIGHT);
        ChessPiece bishop = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.BISHOP);
        ChessPiece queen = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.QUEEN);
        ChessPiece king = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KING);
        ChessPiece pawn = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.PAWN);

        ChessPiece wRook = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.ROOK);
        ChessPiece wKnight = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KNIGHT);
        ChessPiece wBishop = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.BISHOP);
        ChessPiece wQueen = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.QUEEN);
        ChessPiece wKing = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KING);
        ChessPiece wPawn = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);

        board = new ChessPiece[][]{
                {wRook, wKnight, wBishop, wQueen, wKing, wBishop.copy(), wKnight.copy(), wRook.copy()},
                {wPawn, wPawn.copy(), wPawn.copy(), wPawn.copy(), wPawn.copy(), wPawn.copy(), wPawn.copy(), wPawn.copy()},
                new ChessPiece[8],
                new ChessPiece[8],
                new ChessPiece[8],
                new ChessPiece[8],
                {pawn, pawn.copy(), pawn.copy(), pawn.copy(), pawn.copy(), pawn.copy(), pawn.copy(), pawn.copy()},
                {rook, knight, bishop, queen, king, bishop.copy(), knight.copy(), rook.copy()},
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {return true;}
        if (o == null || getClass() != o.getClass()) {return false;}
        ChessBoard comp = (ChessBoard)o;
        return Objects.deepEquals(this.board, comp.board);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(this.board);
    }

    @Override
    public String toString() {
        StringBuilder tempBoard = new StringBuilder();
        for (ChessPiece[] row : this.board) {
            StringBuilder tempColumn = new StringBuilder();
            tempColumn.append("|");
            for (ChessPiece col : row) {
                if (col == null) {
                    tempColumn.append(" ");
                } else {
                    tempColumn.append(col);
                }
                tempColumn.append("|");
            }
            tempBoard.append(tempColumn);
            tempBoard.append("\n");
        }
        return tempBoard.toString();
    }
}
