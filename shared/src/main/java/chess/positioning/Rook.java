package chess.positioning;

import chess.*;

public class Rook extends Calculator {

    public Rook(ChessBoard board, ChessPosition pos, ChessPiece piece) {
          super(board, pos, piece, new int[][] {{1, 0},{0,1},{-1,0},{0,-1}}, 8);
    }
}
