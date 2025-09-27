package chess.positioning;

import chess.*;

public class Queen extends Calculator {

    public Queen(ChessBoard board, ChessPosition pos, ChessPiece piece) {
        super(board, pos, piece, new int[][] {{1, 0},{0,1},{-1,0},{0,-1},{1,1},{1,-1},{-1,1},{-1,-1}}, 8);
    }
}
