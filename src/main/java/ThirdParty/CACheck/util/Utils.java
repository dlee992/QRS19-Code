package ThirdParty.CACheck.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import ThirdParty.CACheck.AMSheet;
import ThirdParty.CACheck.CellArray;
import ThirdParty.CACheck.R1C1Cell;
import ThirdParty.CACheck.cellarray.inference.FormulaPattern;

public class Utils {

	public static String exprDir() {
		String osName = System.getProperty("os.name");
		String dir = null;
		if (osName.toLowerCase().contains("windows")) {
			dir = "D:/research/spreadsheets/experiment/";
		} else {
			dir = "/home/wsdou/amcheck/";
		}
		return dir;
	}

	public static List<List<Cell>> extractCells(HSSFSheet sheet) {

		int rowNum = 0;
		int columnNum = 0;
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

		List<List<Cell>> cells = new ArrayList<List<Cell>>();
		for (int i = 0; i < rowNum; i++) {
			Row row = sheet.getRow(i);
			ArrayList<Cell> rowCells = new ArrayList<Cell>();
			if (row == null) {
				for (int j = 0; j < columnNum; j++) {
					rowCells.add(null);
				}
			} else {
				for (int j = 0; j < columnNum; j++) {
					cell = row.getCell(j);
					rowCells.add(cell);
				}
			}
			cells.add(rowCells);
		}

		return cells;
	}

	public static AMSheet extractSheet(Sheet sheet, String xlsName) {
		AMSheet s = new AMSheet(sheet, xlsName);
		return s;
	}

	/**
	 * There are some errors here, later, it should be refactored.
	 */
	public static String convertA1ToR1C1(int row, int column, String formula) {
		StringBuffer result = new StringBuffer();
		for (int i = 0; i < formula.length();) {
			int start = i;
			int end = i;
			// external worksheet
			if (formula.charAt(i) == '\'') {
				i++;
				while (i < formula.length() && formula.charAt(i) != '\'')
					i++;
				i += 2; // skip the '!'
				end = i;

				result.append(formula.substring(start, end));
				continue;
			} else if (!isLetter(formula.charAt(i)) && formula.charAt(i) != '$') {
				// the basic operations for excel
				for (; i < formula.length()
						&& (!isLetter(formula.charAt(i))
								&& formula.charAt(i) != '$' && formula
								.charAt(i) != '\''); i++)
					;
				end = i;

				result.append(formula.substring(start, end));
				continue;
			} else {
				if (isLetter(formula.charAt(i)) || formula.charAt(i) == '$') {
					// deal with the "row"
					if (formula.charAt(i) == '$') {
						i++; // skip the $
					}
					for (; i < formula.length() && isLetter(formula.charAt(i)); i++)
						;

					if (isNumber(formula.charAt(i)) || formula.charAt(i) == '$') {
						// deal with the "column"
						if (formula.charAt(i) == '$') {
							i++;
						}
						for (; i < formula.length()
								&& isNumber(formula.charAt(i)); i++)
							;
						end = i;
						result.append(extractCell(row, column,
								formula.substring(start, end)));
					} else { // some func, not row number.
						end = i;

						result.append(formula.substring(start, end));
						continue;
					}
				}
			}
		}

		return result.toString();
	}

	public static boolean isLetter(char c) {
		return c >= 'A' && c <= 'Z';
	}

	public static boolean isNumber(char c) {
		return c >= '0' && c <= '9';
	}

	public static R1C1Cell extractCell(int row, int column, String ref) {
		String sCol = null;
		String sRow = null;
		boolean colRelative = true;
		boolean rowRelative = true;
		int i;
		if (ref.startsWith("$")) {
			for (i = 1; i < ref.length() && isLetter(ref.charAt(i)); i++)
				;
			sCol = ref.substring(1, i);
			colRelative = false;
		} else {
			for (i = 0; i < ref.length() && isLetter(ref.charAt(i)); i++)
				;
			sCol = ref.substring(0, i);
		}

		if (ref.charAt(i) == '$') {
			int start = i + 1;
			for (i = i + 1; i < ref.length() && isNumber(ref.charAt(i)); i++)
				;
			sRow = ref.substring(start, i);
			rowRelative = false;
		} else {
			int start = i;
			for (; i < ref.length() && isNumber(ref.charAt(i)); i++)
				;
			sRow = ref.substring(start, i);
		}

		int iCol = 0;
		for (int j = 0; j < sCol.length(); j++) {
			iCol = iCol * 26 + sCol.charAt(j) - 'A' + 1;
		}

		int iRow = Integer.parseInt(sRow);

		R1C1Cell cell = new R1C1Cell();
		cell.curRow = row;
		cell.curColumn = column;
		cell.row = iRow - 1;
		cell.rowRelative = rowRelative;
		cell.column = iCol - 1;
		cell.columnRelative = colRelative;

		return cell;
	}

	/**
	 * There are some errors here, later, it should be refactored.
	 */
	public static List<R1C1Cell> extractParameters(int row, int column,
			String formula) {
		List<R1C1Cell> paras = new ArrayList<R1C1Cell>();

		StringBuffer sheet = new StringBuffer();
		boolean isRange = false;
		R1C1Cell preCell = null;

		for (int i = 0; i < formula.length();) {
			int start = i;
			int end = i;
			// external worksheet
			if (formula.charAt(i) == '\'') {
				i++;
				while (i < formula.length() && formula.charAt(i) != '\'')
					i++;
				i += 2; // skip the '!'
				end = i;

				sheet.append(formula.substring(start, end));
				continue;
			} else if (!isLetter(formula.charAt(i)) && formula.charAt(i) != '$') {
				// the basic operations for excel
				for (; i < formula.length()
						&& (!isLetter(formula.charAt(i))
								&& formula.charAt(i) != '$' && formula
								.charAt(i) != '\''); i++)
					;
				end = i;

				// result.append(formula.substring(start, end));
				continue;
			} else { // a new inputs cells.
				R1C1Cell cell = null;

				if (isLetter(formula.charAt(i)) || formula.charAt(i) == '$') {
					// deal with the "row"
					if (formula.charAt(i) == '$') {
						i++; // skip the $
					}
					for (; i < formula.length() && isLetter(formula.charAt(i)); i++)
						;
					if (isNumber(formula.charAt(i)) || formula.charAt(i) == '$') {
						// deal with the "column"
						if (formula.charAt(i) == '$') {
							i++;
						}
						for (; i < formula.length()
								&& isNumber(formula.charAt(i)); i++)
							;
						end = i;

						cell = extractCell(row, column,
								formula.substring(start, end));
					} else {
						// some excel function.
						end = i;
						// result.append(formula.substring(start, end));
						continue;
					}
				}
				if (cell != null) {
					if (isRange == true) {
						List<R1C1Cell> cells = getRangeCells(preCell, cell);
						for (R1C1Cell c : cells) {
							paras.add(c);
						}

						isRange = false;
						preCell = null;
					} else if (i < formula.length() && formula.charAt(i) == ':') {
						isRange = true;
						preCell = cell;
					} else {
						paras.add(cell);
					}

					sheet = new StringBuffer();
					cell = null;
				}
			}
		}

		// delete duplicated cells, for performance.
		Set<R1C1Cell> tmp = new HashSet<R1C1Cell>();
		tmp.addAll(paras);
		paras.clear();
		paras.addAll(tmp);

		return paras;
	}

	private static List<R1C1Cell> getRangeCells(R1C1Cell preCell,
			R1C1Cell curCell) {
		List<R1C1Cell> cells = new ArrayList<R1C1Cell>();

		if (preCell.row != curCell.row) {
			for (int row = preCell.row; row <= curCell.row; row++) {
				R1C1Cell cell = new R1C1Cell();

				cell.curRow = preCell.curRow;
				cell.curColumn = preCell.curColumn;
				cell.row = row;
				cell.column = preCell.column;
				cell.rowRelative = preCell.rowRelative;
				cell.columnRelative = preCell.columnRelative;

				cells.add(cell);
			}
		} else {
			for (int col = preCell.column; col <= curCell.column; col++) {
				R1C1Cell cell = new R1C1Cell();

				cell.curRow = preCell.curRow;
				cell.curColumn = preCell.curColumn;
				cell.row = preCell.row;
				cell.column = col;
				cell.rowRelative = preCell.rowRelative;
				cell.columnRelative = preCell.columnRelative;

				cells.add(cell);
			}
		}

		return cells;
	}

	public static boolean isValidInput(AMSheet sheet, CellArray ca,
			Set<R1C1Cell> inputs, Cell curCell) {
		for (R1C1Cell input : inputs) {
			R1C1Cell trueInput = ca.getTrueInput(curCell, input);

			if (trueInput.row < 0 || trueInput.column < 0) {
				return false;
			}

			if (trueInput.row >= sheet.getRowNum()
					|| trueInput.column >= sheet.getColumnNum()) {
				return false;
			}

			try {
				getNumericalValue(sheet, ca, input, curCell);
			} catch (Exception e) {
				return false;
			}
		}
		return true;
	}

	public static boolean isValidOutput(Cell cell) {
		// When the cell's empty or empty string, we ignore it.

		if (CellUtils.isBlank(cell) || CellUtils.isDate(cell)
				|| CellUtils.isString(cell)) {
			return false;
		}
		try {
			getNumericalValue(cell);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	public static double getNumericalValue(AMSheet sheet, CellArray ca,
			R1C1Cell input, Cell curCell) throws Exception {
		R1C1Cell trueInput = ca.getTrueInput(curCell, input);
		Cell c = sheet.getCell(trueInput.row, trueInput.column);
		return getNumericalValue(c);
	}

	public static double getNumericalValue(Cell c) throws Exception {
		if (c != null && CellUtils.isString(c)) {
			String val = c.getStringCellValue();
			if (val == null || val.trim().equals("")) {
				return 0.0;
			}
			// Fix a bug about /. [2014-10-17]. It is not sure return 0 or 1
			// For some cases, I'm not sure it is a number or not.
			// Ignore all. I don't care this specific case. [2014-10-19]
			// TODO

			if (val.equals("-") || val.equals("/")) {
				return 0.0;
			}

			throw new Exception("Not a number");
		}
		double data = 0;
		if (c != null) {
			data = c.getNumericCellValue();
		}
		return data;
	}

	public static boolean isEmpty(Cell c) {
		if (CellUtils.isBlank(c)) {
			return true;
		}

		if (CellUtils.isString(c)) {
			String val = c.getStringCellValue();
			if (val == null || val.equals("")) {
				return true;
			}
		}
		return false;
	}

	// judge whether some relative addresses have been changed into absolute
	// addresses.
	public static boolean referChanged(List<Object> spec) {
		Set<R1C1Cell> inputs = FormulaPattern.getInputs(spec);
		for (R1C1Cell input : inputs) {
			if (input.referChanged) {
				return true;
			}
		}

		return false;
	}

	public static boolean hasPlainValue(AMSheet sheet, CellArray ca) {
		for (int index = 0; index < ca.size(); index++) {
			Cell cell = ca.getCell(sheet, index);
			if (isValidOutput(cell) && !CellUtils.isFormula(cell)) {
				return true;
			}
		}

		return false;
	}
}
