package clustering.smellDetectionClustering;

import java.util.Objects;

public class FakeCell {

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }

    int row, column;

    public FakeCell(int row, int column) {
        this.row = row;
        this.column = column;
    }

    @Override
    public String toString() {
        return "FakeCell{" +
                "row=" + row +
                ", column=" + column +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FakeCell fakeCell = (FakeCell) o;
        return row == fakeCell.row &&
                column == fakeCell.column;
    }

    @Override
    public int hashCode() {

        return Objects.hash(row, column);
    }
}
