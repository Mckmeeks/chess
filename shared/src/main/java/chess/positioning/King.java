package chess.positioning;

import chess.*;

public class King extends Calculator {

    public King(ChessBoard board, ChessPosition pos, ChessPiece piece) {
        super(board, pos, piece, new int[][] {{1, 0},{0,1},{-1,0},{0,-1},{1,1},{1,-1},{-1,1},{-1,-1}}, 1);
    }
}
