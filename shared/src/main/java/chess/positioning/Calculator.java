package chess.positioning;

import java.util.HashSet;

import chess.*;

public class Calculator {

    protected int[][] validDirections;
    protected int maxSquares;

    public ChessBoard board;
    public ChessPosition pos;
    public ChessPiece piece;

    public Calculator(){}

    public Calculator(ChessBoard board, ChessPosition pos, ChessPiece piece, int[][] validDirections, int multipleSquares){
        this.board = board;
        this.pos = pos;
        this.piece = piece;
        this.validDirections = validDirections;
        this.maxSquares = multipleSquares;
    }

    public static Calculator getCalculator(ChessBoard board, ChessPosition pos, ChessPiece piece) {

        return switch (piece.getPieceType()) {
            case ChessPiece.PieceType.ROOK -> new Rook(board, pos, piece);
            case ChessPiece.PieceType.KNIGHT -> new Knight(board, pos, piece);
            case ChessPiece.PieceType.BISHOP -> new Bishop(board, pos, piece);
            case ChessPiece.PieceType.KING -> new King(board, pos, piece);
            case ChessPiece.PieceType.QUEEN -> new Queen(board, pos, piece);
            case ChessPiece.PieceType.PAWN -> new Pawn(board, pos, piece);
            default -> new Calculator(board, pos, piece, new int[][]{{0,0}}, 0);
        };
    }

    public HashSet<ChessMove> getMoves() {
        int row;
        int col;
        HashSet<ChessMove> moves = new HashSet<>();

        for (int[] dir : this.validDirections) {
            row = this.pos.getRow();
            col = this.pos.getColumn();

            for (int i = 1; i <= maxSquares; i++) {
                ChessPosition tempPos = new ChessPosition(row + dir[0] * i, col + dir[1] * i);
                if (!this.onBoard(tempPos)) {break;}
                ChessPiece piece = this.board.getPiece(tempPos);
                if (piece == null) {moves.add(new ChessMove(this.pos, tempPos, null));}
                else {
                    if (!(this.piece.getTeamColor().equals(piece.getTeamColor()))) {
                        moves.add(new ChessMove(this.pos, tempPos, null));}
                    break;
                }
            }
        }
        return moves;
    }

    public ChessPiece.PieceType getType(){
        return this.piece.getPieceType();
    }

    public boolean onBoard(ChessPosition pos){
        return (1 <= pos.getRow() && pos.getRow() <= 8 && 1 <= pos.getColumn() && pos.getColumn() <= 8);
    }
}

