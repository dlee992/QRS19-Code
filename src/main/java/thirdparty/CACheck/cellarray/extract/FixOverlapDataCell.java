package thirdparty.CACheck.cellarray.extract;

import java.io.BufferedWriter;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;

import thirdparty.CACheck.AMSheet;
import thirdparty.CACheck.CellArray;
import thirdparty.CACheck.amcheck.AnalysisPattern;
import thirdparty.CACheck.cellarray.synthesis.SynthesisUtils;
import thirdparty.CACheck.snippet.Snippet;
import thirdparty.CACheck.util.CellUtils;
import thirdparty.CACheck.util.Utils;

public class FixOverlapDataCell {
	public static void fix(AMSheet sheet, List<CAResult> allCARs, AnalysisPattern analysisPattern) {
		if (analysisPattern.overlapDataCell == false
				|| analysisPattern.curPattern != AnalysisPattern.Share_Cells) {
			return;
		}

		for (int i = 0; i < allCARs.size(); i++) {
			CAResult car = allCARs.get(i);
			AmbiguousDetector amDetector = new AmbiguousDetector(sheet, null);
			car.isSameRowOrColumn = amDetector.isSameRowOrColumn(car.cellArray,
					car.pattern);
		}

		for (int i = 0; i < allCARs.size(); i++) {
			CAResult car1 = allCARs.get(i);
			for (int j = i + 1; j < allCARs.size(); j++) {
				CAResult car2 = allCARs.get(j);
				try {
					if (car1.cellArray.isRowCA && !car2.cellArray.isRowCA) {
						check(sheet, car1, car2);
					} else if (!car1.cellArray.isRowCA
							&& car2.cellArray.isRowCA) {
						check(sheet, car2, car1);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void recover(AMSheet sheet, List<CAResult> allCARs,
			List<Snippet> allSnippets, BufferedWriter writer, AnalysisPattern analysisPattern) {
		if (analysisPattern.overlapDataCell == false
				|| analysisPattern.curPattern != AnalysisPattern.Share_Cells) {
			return;
		}

		for (int i = 0; i < allCARs.size(); i++) {
			CAResult car = allCARs.get(i);
			boolean recover = false;
			for (CAResult compCar : car.compensateCAs) {
				if (!allCARs.contains(compCar)) {
					if (car.cellArray.start - 1 == compCar.cellArray.rowOrColumn) {
						car.cellArray.start--;
						recover = true;
					} else if (car.cellArray.end + 1 == compCar.cellArray.rowOrColumn) {
						car.cellArray.end++;
						recover = true;
					}
				}
			}
			if (recover) {
				car.isMissing = true;
				car.isAmbiguous = true;
				AmbiguousDetector amDetector = new AmbiguousDetector(sheet,
						allSnippets);
				amDetector.detect(car, writer, analysisPattern);
			}
		}
	}

	protected static void check(AMSheet sheet, CAResult rowCAR, CAResult colCAR)
			throws Exception {
		// car1 is a row-based cell array
		// car2 is a column-based cell array
		CellArray ca1 = rowCAR.cellArray;
		CellArray ca2 = colCAR.cellArray;

		Cell cell = null;

		if (ca1.start == ca2.rowOrColumn && ca2.end == ca1.rowOrColumn) {
			cell = rowCAR.cellArray.getCell(sheet, 0);
			if (!CellUtils.isNumber(cell) || CellUtils.isFormula(cell)) {
				return;
			}

			if (isRow(sheet, cell, rowCAR, colCAR)) {
				ca2.end = ca2.end - 1;
			} else {
				ca1.start = ca1.start + 1;
			}
		} else if (ca1.start == ca2.rowOrColumn && ca2.start == ca1.rowOrColumn) {
			cell = rowCAR.cellArray.getCell(sheet, 0);
			if (!CellUtils.isNumber(cell) || CellUtils.isFormula(cell)) {
				return;
			}

			if (isRow(sheet, cell, rowCAR, colCAR)) {
				ca2.start = ca2.start + 1;
			} else {
				ca1.start = ca1.start + 1;
			}
		} else if (ca1.end == ca2.rowOrColumn && ca2.end == ca1.rowOrColumn) {
			cell = rowCAR.cellArray.getCell(sheet, ca1.size() - 1);
			if (!CellUtils.isNumber(cell) || CellUtils.isFormula(cell)) {
				return;
			}

			if (isRow(sheet, cell, rowCAR, colCAR)) {
				ca2.end = ca2.end - 1;
			} else {
				ca1.end = ca1.end - 1;
			}
		} else if (ca1.end == ca2.rowOrColumn && ca2.start == ca1.rowOrColumn) {
			cell = rowCAR.cellArray.getCell(sheet, ca1.size() - 1);
			if (!CellUtils.isNumber(cell) || CellUtils.isFormula(cell)) {
				return;
			}

			if (isRow(sheet, cell, rowCAR, colCAR)) {
				ca2.start = ca2.start + 1;
			} else {
				ca1.end = ca1.end - 1;
			}
		}

		// Further aggressive fix.
		else if (ca1.start < ca2.rowOrColumn && ca1.end > ca2.rowOrColumn
				&& (ca1.rowOrColumn == ca2.end || ca1.rowOrColumn == ca2.start)) {
			cell = rowCAR.cellArray.getCell(sheet, ca2.rowOrColumn - ca1.start);
			if (!CellUtils.isNumber(cell) || CellUtils.isFormula(cell)) {
				return;
			}
			if (ca1.rowOrColumn == ca2.end) {
				if (isRow(sheet, cell, rowCAR, colCAR)) {
					ca2.end = ca2.end - 1;
				}
			} else if (ca1.rowOrColumn == ca2.start) {
				if (isRow(sheet, cell, rowCAR, colCAR)) {
					ca2.start = ca2.start + 1;
				}
			}
		} else if (ca2.start < ca1.rowOrColumn && ca2.end > ca1.rowOrColumn
				&& (ca2.rowOrColumn == ca1.end || ca2.rowOrColumn == ca1.start)) {
			cell = rowCAR.cellArray.getCell(sheet, ca2.rowOrColumn - ca1.start);
			if (!CellUtils.isNumber(cell) || CellUtils.isFormula(cell)) {
				return;
			}
			if (ca2.rowOrColumn == ca1.end) {
				if (!isRow(sheet, cell, rowCAR, colCAR)) {
					ca1.end = ca1.end - 1;
				}
			} else if (ca2.rowOrColumn == ca1.start) {
				if (!isRow(sheet, cell, rowCAR, colCAR)) {
					ca1.start = ca1.start + 1;
				}
			}
		}
	}

	protected static boolean isRow(AMSheet sheet, Cell cell, CAResult rowCAR,
			CAResult colCAR) throws Exception {
		boolean isRow = false;
		if (rowCAR.isSameRowOrColumn
				&& !colCAR.isSameRowOrColumn
				&& !SynthesisUtils.isError(sheet, rowCAR.cellArray, cell,
						rowCAR.pattern)) {
			isRow = true;
		}
		if (!rowCAR.isSameRowOrColumn
				&& colCAR.isSameRowOrColumn
				&& !SynthesisUtils.isError(sheet, colCAR.cellArray, cell,
						colCAR.pattern)) {
			isRow = false;
		}

		if (!SynthesisUtils.isError(sheet, rowCAR.cellArray, cell,
				rowCAR.pattern)) {
			isRow = true;
		}
		if (!SynthesisUtils.isError(sheet, colCAR.cellArray, cell,
				colCAR.pattern)) {
			isRow = false;
		}

		double value = Utils.getNumericalValue(cell);

		Double rowValue = SynthesisUtils.computeValue(sheet, rowCAR.cellArray,
				cell, rowCAR.pattern);
		Double colValue = SynthesisUtils.computeValue(sheet, colCAR.cellArray,
				cell, colCAR.pattern);

		if (Math.abs(rowValue - value) <= Math.abs(colValue - value)) {
			isRow = true;
		} else {
			isRow = false;
		}

		if (isRow
				&& SynthesisUtils.isError(sheet, colCAR.cellArray, cell,
						colCAR.pattern)) {
			colCAR.compensateCAs.add(rowCAR);
		}
		if (!isRow
				&& SynthesisUtils.isError(sheet, rowCAR.cellArray, cell,
						rowCAR.pattern)) {
			rowCAR.compensateCAs.add(rowCAR);
		}

		return isRow;
	}
}
