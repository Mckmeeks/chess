package chess.PositionCalculator;

import java.util.HashSet;

import chess.*;

public class ChessPositionCalculator {

    protected int[][] validDirections;
    protected int maxSquares;

    public ChessBoard board;
    public ChessPosition pos;
    public ChessPiece piece;

    public ChessPositionCalculator(){}

    public ChessPositionCalculator(ChessBoard board, ChessPosition pos, ChessPiece piece, int[][] validDirections, int multipleSquares){
        this.board = board;
        this.pos = pos;
        this.piece = piece;
        this.validDirections = validDirections;
        this.maxSquares = multipleSquares;
    }

    public static ChessPositionCalculator getCalculator(ChessBoard board, ChessPosition pos, ChessPiece piece) {

        return switch (piece.getPieceType()) {
            case ChessPiece.PieceType.ROOK -> new RookPositionCalculator(board, pos, piece);
            case ChessPiece.PieceType.KNIGHT -> new KnightPositionCalculator(board, pos, piece);
            case ChessPiece.PieceType.BISHOP -> new BishopPositionCalculator(board, pos, piece);
            case ChessPiece.PieceType.KING -> new KingPositionCalculator(board, pos, piece);
            case ChessPiece.PieceType.QUEEN -> new QueenPositionCalculator(board, pos, piece);
            case ChessPiece.PieceType.PAWN -> new PawnPositionCalculator(board, pos, piece);
            default -> new ChessPositionCalculator(board, pos, piece, new int[][]{{0,0}}, 0);
        };
    }

    public HashSet<ChessMove> getMoves() {
        int row;
        int col;
        HashSet<ChessMove> moves = new HashSet<>();

        for (int[] dir : validDirections) {
            row = pos.getRow();
            col = pos.getColumn();

            for (int i = 1; i <= maxSquares; i++) {
                ChessPosition tempPos = new ChessPosition(row + dir[0] * i, col + dir[1] * i);
                if (this.onBoard(tempPos)) {
                    ChessPiece piece = board.getPiece(tempPos);
                    if (piece == null) {
                        moves.add(new ChessMove(this.pos, tempPos, null));
                    } else {
                        if (!(this.piece.getTeamColor().equals(piece.getTeamColor()))) {
                            moves.add(new ChessMove(this.pos, tempPos, null));
                        }
                        break;
                    }
                } else break;
            }
        }
        return moves;
    }

    public ChessPiece.PieceType getType(){
        return this.piece.getPieceType();
    }

    private boolean onBoard(ChessPosition pos){
        return (1 <= pos.getRow() && pos.getRow() <= 8 && 1 <= pos.getColumn() && pos.getColumn() <= 8);
    }
}

