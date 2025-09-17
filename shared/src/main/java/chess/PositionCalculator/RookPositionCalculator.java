package chess.PositionCalculator;

import chess.*;

public class RookPositionCalculator extends ChessPositionCalculator {

    public RookPositionCalculator(ChessBoard board, ChessPosition pos, ChessPiece piece) {
          super(board, pos, piece, new int[][] {{1, 0},{0,1},{-1,0},{0,-1}}, 8);
    }
}
