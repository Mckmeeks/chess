package chess.positioning;

import chess.*;

public class Bishop extends Calculator {

    public Bishop(ChessBoard board, ChessPosition pos, ChessPiece piece) {
        super(board, pos, piece, new int[][] {{1, 1},{1,-1},{-1,1},{-1,-1}}, 8);
    }
}
