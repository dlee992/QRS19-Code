package thirdparty.CACheck;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Stack;

public class R1C1Cell implements Serializable {
	
	public static final long serialVersionUID = 123;
	
	// start from 0.
	public int curRow;
	// start from 0.
	public int curColumn;

	// start from 0.
	public int row;
	public boolean rowRelative = false;
	// start from 0.
	public int column;
	public boolean columnRelative = false;

	public boolean referChanged = false;

	public String toString() {
		StringBuffer result = new StringBuffer();
		result.append("R");
		if (rowRelative) {
			if (row != curRow) {
				result.append("[" + (row - curRow) + "]");
			}
		} else {
			result.append(row + 1);
		}

		result.append("C");
		if (columnRelative) {
			if (column != curColumn) {
				result.append("[" + (column - curColumn) + "]");
			}
		} else {
			result.append(column + 1);
		}

		return result.toString();
	}

	public R1C1Cell getTrueCell(int curRow, int curColumn) {
		R1C1Cell cell = new R1C1Cell();

		cell.curRow = curRow;
		cell.curColumn = curColumn;
		cell.rowRelative = rowRelative;
		cell.columnRelative = columnRelative;
		if (this.rowRelative) {
			cell.row = cell.curRow + this.row - this.curRow;
		} else {
			cell.row = this.row;
		}
		if (this.columnRelative) {
			cell.column = cell.curColumn + this.column - this.curColumn;
		} else {
			cell.column = this.column;
		}

		return cell;
	}

	public String getA1Cell(int curRow, int curCol) {
		R1C1Cell cell = getTrueCell(curRow, curCol);
		return cell.getA1Cell();
	}

	public String getA1Cell() {
		Stack<Integer> cols = new Stack<Integer>();
		cols.push(column + 1);
		while (true) {
			int top = cols.pop();
			if (top <= 26) {
				cols.push(top);
				break;
			}
			int i = top / 26;
			int j = top % 26;
			cols.push(j);
			cols.push(i);
		}

		StringBuffer sb = new StringBuffer();
		if (columnRelative == false) {
			sb.append('$');
		}
		while (!cols.empty()) {
			sb.append((char) ('A' + cols.pop() - 1));
		}
		if (rowRelative == false) {
			sb.append('$');
		}
		sb.append(row + 1);
		return sb.toString();
	}
	
	public String getRelativeA1Cell() {
		Stack<Integer> cols = new Stack<Integer>();
		cols.push(column + 1);
		while (true) {
			int top = cols.pop();
			if (top <= 26) {
				cols.push(top);
				break;
			}
			int i = top / 26;
			int j = top % 26;
			cols.push(j);
			cols.push(i);
		}

		StringBuffer sb = new StringBuffer();
		while (!cols.empty()) {
			sb.append((char) ('A' + cols.pop() - 1));
		}
		sb.append(row + 1);
		return sb.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof R1C1Cell) {
			return obj.toString().equals(toString());
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	// It should be deleted later.
	public static Comparator<R1C1Cell> getComparator() {
		return new Comparator<R1C1Cell>() {
			public int compare(R1C1Cell c1, R1C1Cell c2) {
				// absolute address.
				if (c1.rowRelative == false && c2.rowRelative == false
						&& c1.columnRelative == false
						&& c2.columnRelative == false) {
					if (c1.row == c2.row && c1.column == c2.column) {
						return 0;
					} else {
						return 1;
					}
				}

				// absolute address and relative address.
				if (c1.rowRelative == true && c2.rowRelative == false
						|| c1.rowRelative == false && c2.rowRelative == true
						|| c1.columnRelative == true
						&& c2.columnRelative == false
						|| c1.columnRelative == false
						&& c2.columnRelative == true) {
					return 1;
				}

				// other cases.
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
}
