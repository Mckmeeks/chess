package chess.PositionCalculator;

import chess.*;

public class KingPositionCalculator extends ChessPositionCalculator {

    public KingPositionCalculator(ChessBoard board, ChessPosition pos, ChessPiece piece) {
        super(board, pos, piece, new int[][] {{1, 0},{0,1},{-1,0},{0,-1},{1,1},{1,-1},{-1,1},{-1,-1}}, 1);
    }
}
