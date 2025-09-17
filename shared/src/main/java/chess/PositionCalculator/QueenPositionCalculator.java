package chess.PositionCalculator;

import chess.*;

public class QueenPositionCalculator extends ChessPositionCalculator {

    public QueenPositionCalculator(ChessBoard board, ChessPosition pos, ChessPiece piece) {
        super(board, pos, piece, new int[][] {{1, 0},{0,1},{-1,0},{0,-1},{1,1},{1,-1},{-1,1},{-1,-1}}, 8);
    }
}
