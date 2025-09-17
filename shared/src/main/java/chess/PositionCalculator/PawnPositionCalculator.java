package chess.PositionCalculator;

import chess.*;

public class PawnPositionCalculator extends ChessPositionCalculator {

    public PawnPositionCalculator(ChessBoard board, ChessPosition pos, ChessPiece piece) {
        super(board, pos, piece, new int[][] {{1, 0}}, 1);
    }

//    @Override

}
