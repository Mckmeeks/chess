package chess.PositionCalculator;

import chess.*;
import java.util.HashSet;

public class Pawn extends Calculator {

    private int start;
    private int dir;

    public Pawn(ChessBoard board, ChessPosition pos, ChessPiece piece) {
        super(board, pos, piece, new int[][] {{0,0}}, 0);
        switch (piece.getTeamColor()) {
            case ChessGame.TeamColor.WHITE -> {
                start = 2;
                dir = 1;
            }
            case ChessGame.TeamColor.BLACK -> {
                start = 7;
                dir = -1;
            }
        }
    }

    @Override
    public HashSet<ChessMove> getMoves() {
        HashSet<ChessMove> tempMoves = new HashSet<>();
        ChessPosition tempPos = new ChessPosition(this.pos.getRow() + dir, this.pos.getColumn());
        if (onBoard(tempPos)) {
            if (this.board.getPiece(tempPos) == null) {
                tempMoves.addAll(getPromotions(tempPos));
                if (this.pos.getRow() == start) {
                    tempPos = new ChessPosition(this.pos.getRow() + 2 * dir, this.pos.getColumn());
                    if (this.board.getPiece(tempPos) == null) tempMoves.add(new ChessMove(this.pos, tempPos, null));
                }
            }
        }
        for (int cor : new int[] {-1,1}) {
            tempPos = new ChessPosition(this.pos.getRow() + dir, this.pos.getColumn() + cor);
            if (onBoard(tempPos)) {
                ChessPiece tempPiece = this.board.getPiece(tempPos);
                if (tempPiece != null && (tempPiece.getTeamColor() != this.piece.getTeamColor())) {
                    tempMoves.addAll(getPromotions(tempPos));
                }
            }
        }
        return tempMoves;
    }

    private HashSet<ChessMove> getPromotions(ChessPosition tempPos) {
        HashSet<ChessMove> tempMoves = new HashSet<>();
        if (tempPos.getRow() == 1 || tempPos.getRow() == 8) {
            for (ChessPiece.PieceType type : new ChessPiece.PieceType[] {ChessPiece.PieceType.KNIGHT, ChessPiece.PieceType.QUEEN, ChessPiece.PieceType.BISHOP, ChessPiece.PieceType.ROOK}) {
                tempMoves.add(new ChessMove(this.pos, tempPos, type));
            }
        } else tempMoves.add(new ChessMove(this.pos, tempPos, null));
        return tempMoves;
    }
}
