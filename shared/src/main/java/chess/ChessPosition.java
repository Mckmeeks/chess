package chess;

import java.util.Objects;

/**
 * Represents a single square position on a chess board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPosition {

    private int Row;
    private int Col;

    public ChessPosition(int row, int col) {
        setRow(row);
        setColumn(col);
    }

    /**
     * @return which row this position is in
     * 1 codes for the bottom row
     */
    public int getRow() {
        return this.Row;
    }

    /**
     * @return which column this position is in
     * 1 codes for the left row
     */
    public int getColumn() {
        return this.Col;
    }

    public void setRow(int row) {
        this.Row = row;
    }

    public void setColumn(int col) {
        this.Col = col;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        ChessPosition comp = (ChessPosition)o;
        return (this.Row == comp.Row && this.Col == comp.Col);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.Row, this.Col);
    }

    @Override
    public String toString() {
        return String.format("(%d,%d)", this.Row, this.Col);
    }
}
