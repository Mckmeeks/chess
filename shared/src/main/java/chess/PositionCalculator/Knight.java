package chess.PositionCalculator;

import chess.*;

public class Knight extends Calculator {

    public Knight(ChessBoard board, ChessPosition pos, ChessPiece piece) {
        super(board, pos, piece, new int[][] {{1,2},{1,-2},{-1,2},{-1,-2},{2,1},{2,-1},{-2,1},{-2,-1}}, 1);
    }
}
