package chess.PositionCalculator;

import chess.*;

import java.util.HashSet;

public class PawnPositionCalculator extends ChessPositionCalculator {

    public PawnPositionCalculator(ChessBoard board, ChessPosition pos, ChessPiece piece) {
        super(board, pos, piece, new int[][]{{1, 0}, {0, 1}, {0, -1}}, 1);
    }

    @Override
    public HashSet<ChessMove> getMoves() {
        int row = this.pos.getRow();
        int col = this.pos.getColumn();
        int dir;
        int start;
        HashSet<ChessMove> moves = new HashSet<>();

        start = switch (this.piece.getTeamColor()) {
            case ChessGame.TeamColor.BLACK -> {
                dir = -1;
                yield 7;
            }
            case ChessGame.TeamColor.WHITE -> {
                dir = 1;
                yield 2;
            }
        };


        ChessMove move = refactorCheckSpace(row + dir, col);
        if (move != null) {
            if (this.pos.getRow() == start) {
                moves.add(move);
                ChessMove move2 = refactorCheckSpace(row + dir * 2, col);
                if (move2 != null) moves.add(move2);
            } else {
                moves.addAll(promotions(new ChessPosition(row + dir, col)));
            }
        }


        ChessPosition side;
        for (int i : new int[]{-1, 1}) {
            side = new ChessPosition(row + dir, col - i);
            if (this.onBoard(side)) {
                ChessPiece tempPiece = this.board.getPiece(side);
                if (tempPiece != null && !(this.piece.getTeamColor().equals(tempPiece.getTeamColor()))) {
                    moves.addAll(promotions(side));
                }
            }
        }
        return moves;
    }

    private ChessMove refactorCheckSpace(int row, int col) {
        ChessPosition newPos = new ChessPosition(row, col);
        if (this.onBoard(newPos)) {
            if (this.board.getPiece(newPos) == null) {
                return new ChessMove(this.pos, newPos, null);
            }
        }
        return null;
    }

    private HashSet<ChessMove> promotions(ChessPosition pos) {
        HashSet<ChessMove> moves = new HashSet<>();
        if (pos.getRow() == 1 || pos.getRow() == 8) {
            for (ChessPiece.PieceType type : ChessPiece.PieceType.values()) {
                if (type != ChessPiece.PieceType.KING && type != ChessPiece.PieceType.PAWN) {
                    moves.add(new ChessMove(this.pos, pos, type));
                }
            }
        } else moves.add(new ChessMove(this.pos, pos, null));
        return moves;
    }
}
