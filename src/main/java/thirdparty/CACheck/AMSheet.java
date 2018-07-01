package thirdparty.CACheck;

import java.util.Iterator;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import thirdparty.CACheck.snippet.Snippet;
import thirdparty.CACheck.util.CellUtils;

public class AMSheet {
	private Sheet sheet = null;

	private Cell[][] cells;

	private int rowNum;

	private int columnNum;

	private Layout[][] layouts;
	
	public String xslName = null;

	public AMSheet(Sheet sheet, String xslName) {
		this.sheet = sheet;
		this.xslName = xslName;

		Iterator<Row> rowIterator = sheet.iterator();
		Cell cell = null;
		while (rowIterator.hasNext()) {
			Row row = rowIterator.next();
			Iterator<Cell> cellIterator = row.cellIterator();
			while (cellIterator.hasNext()) {
				cell = cellIterator.next();
			}
			if (cell != null) {
				columnNum = columnNum > cell.getColumnIndex() + 1 ? columnNum
						: cell.getColumnIndex() + 1;
			}
		}

		if (cell != null) {
			rowNum = cell.getRowIndex() + 1;
		}

		populateCells();
		populateLayouts();
	}

	public Sheet getSheet() {
		return sheet;
	}

	public Cell getCell(int row, int column) {
		if (row >= rowNum || column >= columnNum) {
			return null;
		}
		return cells[row][column];
	}
	
	public Cell[][] getAllCell() {
		return cells;
	}

	public int getRowNum() {
		return rowNum;
	}

	public int getColumnNum() {
		return columnNum;
	}

	public Layout[][] getLayouts() {
		return layouts;
	}

	public boolean isFence(int num, boolean isRow, Snippet dr) {
		if (isRow) {
			boolean fence = true;
			int row = num;
			for (int col = dr.left; col <= dr.right; col++) {
				if (!(layouts[row][col] == Layout.BLANK || layouts[row][col] == Layout.STRING)) {
					fence = false;
					break;
				}
			}
			return fence;
		} else {
			boolean fence = true;
			int col = num;
			for (int row = dr.up; row <= dr.bottom; row++) {
				if (!(layouts[row][col] == Layout.BLANK || layouts[row][col] == Layout.STRING)) {
					fence = false;
					break;
				}
			}
			return fence;
		}
	}

	public boolean noLabel(Snippet dr) {
		for (int row = dr.up; row <= dr.bottom; row++) {
			for (int col = dr.left; col <= dr.right; col++) {
				if (layouts[row][col] == Layout.STRING) {
					return false;
				}
			}
		}
		return true;
	}

	public boolean isBlack(int num, boolean isRow) {
		boolean blank = true;
		if (num < 0 || isRow && num >= rowNum || !isRow && num >= columnNum) {
			return true;
		}

		if (isRow) {
			int row = num;
			for (int col = 0; col < columnNum; col++) {
				if (!(layouts[row][col] == Layout.BLANK)) {
					blank = false;
					break;
				}
			}
		} else {
			int col = num;
			for (int row = 0; row < rowNum; row++) {
				if (!(layouts[row][col] == Layout.BLANK)) {
					blank = false;
					break;
				}
			}
		}

		return blank;
	}

	private void populateCells() {
		cells = new Cell[rowNum][columnNum];
		for (int i = 0; i < rowNum; i++) {
			Row row = sheet.getRow(i);
			if (row == null) {
				for (int j = 0; j < columnNum; j++) {
					cells[i][j] = null;
				}
			} else {
				for (int j = 0; j < columnNum; j++) {
					Cell cell = row.getCell(j);
					cells[i][j] = cell;
				}
			}
		}
	}

	private void populateLayouts() {
		layouts = new Layout[rowNum][columnNum];
		for (int i = 0; i < rowNum; i++) {
			for (int j = 0; j < columnNum; j++) {
				if (CellUtils.isBlank(cells[i][j])) {
					layouts[i][j] = Layout.BLANK;
				} else if (CellUtils.isString(cells[i][j])
						|| CellUtils.isDate(cells[i][j])) {
					layouts[i][j] = Layout.STRING;
				} else {
					layouts[i][j] = Layout.NUMBER;
				}
			}
		}
	}
}
