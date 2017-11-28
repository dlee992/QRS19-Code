package entity;

import java.util.Comparator;

public class R1C1Cell {
	public int curRow;
	public int curColumn;

	public int row;
	public boolean rowRelative = false;
	public int column;
	public boolean columnRelative = false;

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append("R");
		if (rowRelative) {
			if (row != curRow) {
				result.append("[").append(row - curRow).append("]");
			}
		} else {
			result.append(row);
		}

		result.append("C");
		if (columnRelative) {
			if (column != curColumn) {
				result.append("[").append(column - curColumn).append("]");
			}
		} else {
			result.append(column);
		}

		return result.toString();
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof R1C1Cell && obj.toString().equals(toString());
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	public static Comparator<R1C1Cell> getComparator() {
		return new Comparator<R1C1Cell>() {
			public int compare(R1C1Cell c1, R1C1Cell c2) {
				int row1 = c1.row - c1.curRow;
				int col1 = c1.column - c1.curColumn;
				int row2 = c2.row - c2.curRow;
				int col2 = c2.column - c2.curColumn;

				if (row1 == row2 && col1 == col2) {
					return 0;
				} else if (row1 == row2 && col1 < col2) {
					return -1;
				} else if (row1 == row2 && col1 > col2) {
					return 1;
				} else if (row1 < row2 && col1 == col2) {
					return -1;
				} else if (row1 > row2 && col1 == col2) {
					return 1;
				} else {
					return -1;
				}
			}
		};
	}

	public R1C1Cell getTrueCell(int row, int col) {
		R1C1Cell cell = new R1C1Cell();

		cell.curRow = row;
		cell.curColumn = col;
		cell.row = cell.curRow + this.row - this.curRow;
		cell.column = cell.curColumn + this.column - this.curColumn;

		return cell;
	}
}
