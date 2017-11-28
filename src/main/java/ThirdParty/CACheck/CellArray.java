package ThirdParty.CACheck;

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;

public class CellArray {
	public int rowOrColumn;

	public boolean isRowCA;

	public int start;

	public int end;
	
	public Double precision = null;
	
	// extend the start for transitive cell arrays.
	// TODO not sure.
	public int extendStart = -1;
	
	// Number label.
	// TODO not sure.
	public boolean numberLabel = false;

	// only possible constants now.
	public List<R1C1Cell> constants = new ArrayList<R1C1Cell>();

	public CellArray() {
	}

	public CellArray(int rowOrColumn, boolean isRowCA, int caStart, int caEnd) {
		this.rowOrColumn = rowOrColumn;
		this.isRowCA = isRowCA;
		this.start = caStart;
		this.end = caEnd;
	}

	@Override
	public String toString() {
		String ext;
		if (isRowCA) {
			ext = "[" + rowOrColumn + ", " + start + "] ------ " + "["
					+ rowOrColumn + ", " + end + "]";
		} else {
			ext = "[" + start + ", " + rowOrColumn + "] ------ " + "[" + end
					+ ", " + rowOrColumn + "]";
		}

		return ext;
	}

	public int size() {
		return end - start + 1;
	}

	public Cell getCell(AMSheet sheet, int index) {
		if (index < 0 || index >= size()) {
			throw new ArrayIndexOutOfBoundsException(
					"Cell's index is out of bounds!");
		}
		Cell cell = null;
		if (isRowCA) {
			cell = sheet.getCell(rowOrColumn, start + index);
		} else {
			cell = sheet.getCell(start + index, rowOrColumn);
		}
		return cell;
	}

	public R1C1Cell getTrueInput(Cell curCell, R1C1Cell input) {
		boolean isConstant = false;
		for (R1C1Cell tmp : constants) {
			if (tmp.getA1Cell().equals(input.getA1Cell())) {
				isConstant = true;
				break;
			}
		}
		R1C1Cell trueInput = null;
		if (isConstant) {
			trueInput = input;
		} else {
			trueInput = input.getTrueCell(curCell.getRowIndex(),
					curCell.getColumnIndex());
		}

		return trueInput;
	}
}
