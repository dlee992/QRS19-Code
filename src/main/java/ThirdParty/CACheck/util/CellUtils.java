package ThirdParty.CACheck.util;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;

public class CellUtils {

	// cell is not null
	public static boolean isNumber(Cell cell) {
		if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
			if (!DateUtil.isCellDateFormatted(cell)) {
				return true;
			}
		}
		return false;
	}

	// cell is not null
	public static boolean isFormula(Cell cell) {
		if (cell.getCellType() == Cell.CELL_TYPE_FORMULA) {
			return true;
		}
		return false;
	}

	// cell is not null
	public static boolean isString(Cell cell) {
		if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
			return true;
		}
		return false;
	}

	// cell is not null
	public static boolean isDate(Cell cell) {
		if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC
				&& DateUtil.isCellDateFormatted(cell)) {
			return true;
		}
		return false;
	}

	// cell may be null
	public static boolean isBlank(Cell cell) {
		if (cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK) {
			return true;
		}
		return false;
	}
}
