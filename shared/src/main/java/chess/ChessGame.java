package chess;

import java.util.Collection;
import java.util.Objects;
import java.util.HashSet;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    private TeamColor teamTurn;
    private ChessBoard board;
    private HashSet<ChessPosition> wTeam;
    private HashSet<ChessPosition> bTeam;
    private ChessPosition wKingLoc;
    private ChessPosition bKingLoc;

    public ChessGame() {
        setTeamTurn(TeamColor.WHITE);
        setBoard(new ChessBoard());
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return teamTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        teamTurn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece hero = board.getPiece(startPosition);
        if (hero == null) return null;
        ChessPosition kingPos;
        HashSet<ChessPosition> opTeam;
        if (hero.getTeamColor() == TeamColor.WHITE) {
            kingPos = wKingLoc;
            opTeam = bTeam;
        } else {
            kingPos = bKingLoc;
            opTeam = wTeam;
        }
        var moves = (HashSet<ChessMove>) hero.pieceMoves(board, startPosition);
        HashSet<ChessMove> movesReturnable = new HashSet<>(moves);
        if (hero.getPieceType() != ChessPiece.PieceType.KING) {board.addPiece(startPosition, null);}
        HashSet<ChessMove> dangers = checkCheckBy(opTeam, kingPos);
        for (ChessMove heroMove : moves) {
            for (ChessMove danger : dangers) {
                if (!blocks(heroMove.getEndPosition(), danger)) {movesReturnable.remove(heroMove);}
            }
            if (hero.getPieceType() == ChessPiece.PieceType.KING && !checkCheckBy(opTeam, heroMove.getEndPosition()).isEmpty()) {movesReturnable.remove(heroMove);}
        }
        if (hero.getPieceType() != ChessPiece.PieceType.KING) {board.addPiece(startPosition, hero);}
        return movesReturnable;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        if (validMoves(move.getStartPosition()).contains(move)) {
            ChessPiece temp = board.getPiece(move.getStartPosition());
            board.addPiece(move.getStartPosition(), null);
            if (move.getPromotionPiece() != null) {
                temp = new ChessPiece(temp.getTeamColor(), move.getPromotionPiece());
            }
            board.addPiece(move.getEndPosition(), temp);
        } else {
            throw new InvalidMoveException("Attempted Invalid Move");
        }
    } // add wTeam removal implementation.

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        return (teamColor == TeamColor.WHITE) ? !checkCheckBy(bTeam, wKingLoc).isEmpty() : !checkCheckBy(wTeam, bKingLoc).isEmpty();
    }


    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
//        throw new RuntimeException("Not implemented");
        return false;
    }


    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        return false; //implement
//        throw new RuntimeException("Not implemented");
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
        wTeam = new HashSet<>();
        bTeam = new HashSet<>();
        for (int r = 1; r <= 8; r++) {
            for (int c = 1; c <= 8; c++) {
                ChessPiece temp = board.getPiece(new ChessPosition(r, c));
                if (temp == null) continue;
                if (temp.getTeamColor() == TeamColor.WHITE) {
                    wTeam.add(new ChessPosition(r, c));
                    if (temp.getPieceType() == ChessPiece.PieceType.KING) {wKingLoc = new ChessPosition(r, c);}
                } else {
                    bTeam.add(new ChessPosition(r, c));
                    if (temp.getPieceType() == ChessPiece.PieceType.KING) {bKingLoc = new ChessPosition(r, c);}
                }
            }
        }
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }

    private HashSet<ChessMove> checkCheckBy(HashSet<ChessPosition> team, ChessPosition kingPos) throws UntrackedPieceException {
        var snipers = new HashSet<ChessMove>();
        for (ChessPosition p : team) {
            ChessPiece temp = board.getPiece(p);
            if (temp == null) throw new UntrackedPieceException(String.format("Position %s returned null when in team set.", p));
            for (ChessMove move : temp.pieceMoves(board, p)) {
                if (kingPos.equals(move.getEndPosition())) snipers.add(move);
            }
        }
        return snipers;
    }

    private boolean blocks(ChessPosition pos, ChessMove path) {
        if (pos.equals(path.getStartPosition())) return true; //takes offending piece
        if (path.getStartPosition().getRow() == path.getEndPosition().getRow()) { //blocks path
            return (path.getStartPosition().getColumn() < pos.getColumn() && pos.getColumn() < path.getEndPosition().getColumn()) && (pos.getRow() == path.getEndPosition().getRow());
        }
        else if (path.getStartPosition().getColumn() == path.getEndPosition().getColumn()) {
            return (path.getStartPosition().getRow() < pos.getRow() && pos.getRow() < path.getEndPosition().getRow()) && (pos.getColumn() == path.getEndPosition().getColumn());
        }
        int rowDif = path.getStartPosition().getRow() - path.getEndPosition().getRow();
        int colDif = path.getStartPosition().getColumn() - path.getEndPosition().getColumn();
        if (Math.abs(rowDif) != Math.abs(colDif)) return false;
        else {
            for (int dis = 1; dis <= Math.abs(rowDif); dis++) {
                if (pos.getRow() == path.getStartPosition().getRow() - Integer.signum(rowDif) * dis && pos.getColumn() == path.getStartPosition().getColumn() - Integer.signum(colDif) * dis) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (!(this.getClass().equals(o.getClass()))) return false;
        ChessGame comp = (ChessGame)o;
        return (this.getTeamTurn() == comp.getTeamTurn() && this.getBoard().equals(comp.getBoard()));
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTeamTurn(), getBoard());
    }

    @Override
    public String toString() {
        return getBoard().toString();
    }
}