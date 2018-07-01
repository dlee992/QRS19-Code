package thirdparty.CACheck.util;

import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.RichTextString;

import thirdparty.CACheck.AMSheet;
import thirdparty.CACheck.CellArray;
import thirdparty.CACheck.cellarray.extract.CAResult;
import thirdparty.CACheck.cellarray.synthesis.SynthesisUtils;
import thirdparty.CACheck.formula.ParseFormula;

public class SpreadsheetMark {

	public static void markDetectResult(AMSheet sheet, List<CAResult> allCARs) {
		// markClearStyle(sheet);
		for (CAResult car : allCARs) {
			markClearStyle(sheet, car.cellArray);
			if (car.isAmbiguous == false) {
				SpreadsheetMark.markCorrectCellArray(sheet, car);
			} else {
				SpreadsheetMark.markSmellyCellArray(sheet, car);
				for (Cell cell : car.errorCells) {
					SpreadsheetMark.markFaultyData(sheet, cell);
				}
				for (Cell cell : car.ambiguousCells) {
					if (car.errorCells.contains(cell)) {
						double value = SynthesisUtils.computeValue(sheet,
								car.cellArray, cell, car.pattern);
						SpreadsheetMark.markFixing(sheet, cell, car.pattern,
							true, value);
					} else {
						SpreadsheetMark.markFixing(sheet, cell, car.pattern,
								false, 0.0);
					}
				}
			}
		}
	}

	public static void markClearStyle(AMSheet sheet, CellArray ca) {
		CellStyle pureStyle = sheet.getSheet().getWorkbook().createCellStyle();
		pureStyle.setFillBackgroundColor(IndexedColors.WHITE.getIndex());
		pureStyle.setFillForegroundColor(IndexedColors.WHITE.getIndex());
		pureStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);

		for (int i = 0; i < ca.size(); i++) {
			Cell cell = ca.getCell(sheet, i);
			if (cell != null) {
				cell.setCellStyle(pureStyle);
			}
		}
	}

	public static void markClearStyle(AMSheet sheet) {
		CellStyle pureStype = sheet.getSheet().getWorkbook().createCellStyle();
		pureStype.setFillBackgroundColor(IndexedColors.WHITE.getIndex());
		pureStype.setFillForegroundColor(IndexedColors.WHITE.getIndex());
		pureStype.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);

		for (int i = 0; i < sheet.getRowNum(); i++) {
			for (int j = 0; j < sheet.getColumnNum(); j++) {
				Cell cell = sheet.getCell(i, j);
				if (cell != null) {
					cell.setCellStyle(pureStype);
				}
			}
		}
	}

	public static void markCorrectCellArray(AMSheet sheet, CAResult car) {
		CellStyle style = sheet.getSheet().getWorkbook().createCellStyle();
		if (car.isOverlap) {
			style.setFillBackgroundColor(IndexedColors.LIGHT_GREEN.getIndex());
			style.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
		} else {
			style.setFillBackgroundColor(IndexedColors.GREEN.getIndex());
			style.setFillForegroundColor(IndexedColors.GREEN.getIndex());
		}
		style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);

		CellArray ca = car.cellArray;
		for (int i = 0; i < ca.size(); i++) {
			Cell cell = ca.getCell(sheet, i);
			if (cell != null) {
				cell.setCellStyle(style);
			}
		}
	}

	public static void markSmellyCellArray(AMSheet sheet, CAResult car) {
		CellStyle style = sheet.getSheet().getWorkbook().createCellStyle();
		if (car.isOverlap) {
			style.setFillBackgroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
			style.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
		} else {
			style.setFillBackgroundColor(IndexedColors.YELLOW.getIndex());
			style.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
		}
		style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);

		CellArray ca = car.cellArray;
		for (int i = 0; i < ca.size(); i++) {
			Cell cell = ca.getCell(sheet, i);
			if (Utils.isValidOutput(cell)) {
				cell.setCellStyle(style);
			}
		}
	}

	public static void markFaultyData(AMSheet sheet, Cell cell) {
		CellStyle style = sheet.getSheet().getWorkbook().createCellStyle();
		style.setFillBackgroundColor(IndexedColors.RED.getIndex());
		style.setFillForegroundColor(IndexedColors.RED.getIndex());
		style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);

		if (Utils.isValidOutput(cell)) {
			cell.setCellStyle(style);
		}
	}

	public static void markFixing(AMSheet sheet, Cell cell,
			List<Object> pattern, boolean faulty, double value) {
		String formula = ParseFormula.getA1Pattern(cell.getRowIndex(),
				cell.getColumnIndex(), pattern);
		StringBuffer sb = new StringBuffer();
		if (formula != null && formula.startsWith("(")) {
			// remove the external ()
			formula = formula.substring(1);
			formula = formula.substring(0, formula.length() - 1);
		}
		sb.append("Suggested Repair:");
		sb.append(formula + "\n");
		if (faulty) {
			sb.append("Suggested Value:");
			sb.append(value);
		}

		Comment comment = null;
		RichTextString rts = sheet.getSheet().getWorkbook().getCreationHelper()
				.createRichTextString(sb.toString());
		if (cell.getCellComment() != null) {
			comment = cell.getCellComment();
		} else {
			Drawing drawing = sheet.getSheet().createDrawingPatriarch();
			ClientAnchor anchor = drawing.createAnchor(100, 100, 100, 100, 1,
					1, 6, 5);
			comment = drawing.createCellComment(anchor);
		}
		comment.setString(rts);
		cell.setCellComment(comment);
	}
}
