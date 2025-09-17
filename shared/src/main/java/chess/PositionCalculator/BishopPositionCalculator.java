package chess.PositionCalculator;

import chess.*;

public class BishopPositionCalculator extends ChessPositionCalculator {

    public BishopPositionCalculator(ChessBoard board, ChessPosition pos, ChessPiece piece) {
        super(board, pos, piece, new int[][] {{1, 1},{1,-1},{-1,1},{-1,-1}}, 8);
    }
}
