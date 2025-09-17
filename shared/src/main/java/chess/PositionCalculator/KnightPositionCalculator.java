package chess.PositionCalculator;

import chess.*;

public class KnightPositionCalculator extends ChessPositionCalculator {

    public KnightPositionCalculator(ChessBoard board, ChessPosition pos, ChessPiece piece) {
        super(board, pos, piece, new int[][] {{1,2},{1,-2},{-1,2},{-1,-2},{2,1},{2,-1},{-2,1},{-2,-1}}, 1);
    }
}
