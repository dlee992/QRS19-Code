package ThirdParty.CACheck.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import ThirdParty.CACheck.AMSheet;
import ThirdParty.CACheck.cellarray.extract.CAOverlap;
import ThirdParty.CACheck.cellarray.extract.CAResult;

public class TotalResultWriter {

	public long time = 0;

	public int formula = 0;

	public int excel = 0;
	public int excelProcessed = 0;
	public int excelHasFormula = 0;
	public int excelHasCellArray = 0;
	public int excelHasSmellyCellArray = 0;
	public int excelHasMissingFormulaCellArray = 0;
	public int excelHasInconsistentFormulaCellArray = 0;

	public int worksheet = 0;
	public int worksheetHasFormula = 0;
	public int worksheetHasCellArray = 0;
	public int worksheetHasSmellyCellArray = 0;
	public int worksheetHasMissingFormulaCellArray = 0;
	public int worksheetHasInconsistentFormulaCellArray = 0;

	public int cellArray = 0;
	public int cellArraySameRowOrColumn = 0;
	public int cellArrayFullRowOrColumn = 0;
	public int cellArrayHasConstants = 0;
	public int oppositeCellArray = 0;
	public int overlapCellArray = 0;
	public int allOverlapCellArray = 0;
	public int cellArray2 = 0;
	public int cellArray2SameRowOrColumn = 0;
	public int cellArray2FullRowOrColumn = 0;
	public int correctCellArray = 0;
	public int correctCellArraySameRowOrColumn = 0;
	public int smellyCellArray = 0;
	public int smellyCellArraySameRowOrColumn = 0;
	public int cellArrayMissingFormula = 0;
	public int cellArrayInconsistentFormula = 0;
	public int cellArrayBothSmell = 0;

	public int cellArrayConformanceError100 = 0;
	public int cellArrayConformanceError100Refer = 0;
	public int cellArrayConformanceError101 = 0;
	public int cellArrayConformanceError102 = 0;
	public int cellArrayConformanceError103 = 0;
	public int cellArrayConformanceError104 = 0;
	public int cellConformanceError105 = 0;
	public int cellArrayConformanceError106 = 0;

	public int conformanceError = 0;

	public int fixedInStage1 = 0;

	// index for different category
	public static int categoryRowIndex = 1;

	// indices for the results
	private static int index = 0;
	public static int categoryIndex = index++;

	public static int timeIndex = index++;

	public static int formulaIndex = index++;

	public static int excelIndex = index++;
	public static int excelProcessedIndex = index++;
	public static int excelHasFormulaIndex = index++;
	public static int excelHasCellArrayIndex = index++;
	public static int excelHasSmellyCellArrayIndex = index++;
	public static int excelHasMissingFormulaCellArrayIndex = index++;
	public static int excelHasInconsistentFormulaCellArrayIndex = index++;

	public static int worksheetIndex = index++;
	public static int worksheetHasFormulaIndex = index++;
	public static int worksheetHasCellArrayIndex = index++;
	public static int worksheetHasSmellyCellArrayIndex = index++;
	public static int worksheetHasMissingFormulaCellArrayIndex = index++;
	public static int worksheetHasInconsistentFormulaCellArrayIndex = index++;

	public static int cellArrayIndex = index++;
	public static int cellArraySameRowOrColumnIndex = index++;
	public static int cellArrayFullRowOrColumnIndex = index++;
	public static int cellArrayHasConstantsIndex = index++;
	public static int oppositeCellArrayIndex = index++;
	public static int overlapCellArrayIndex = index++;
	public static int allOverlapCellArrayIndex = index++;
	public static int cellArray2Index = index++;
	public static int cellArray2SameRowOrColumnIndex = index++;
	public static int cellArray2FullRowOrColumnIndex = index++;
	public static int correctCellArrayIndex = index++;
	public static int correctCellArraySameRowOrColumnIndex = index++;
	public static int smellyCellArrayIndex = index++;
	public static int smellyCellArraySameRowOrColumnIndex = index++;
	public static int cellArrayMissingFormulaIndex = index++;
	public static int cellArrayInconsistentFormulaIndex = index++;
	public static int cellArrayBothSmellIndex = index++;

	public static int cellArrayConformanceError100Index = index++;
	public static int cellArrayConformanceError100ReferIndex = index++;
	public static int cellArrayConformanceError101Index = index++;
	public static int cellArrayConformanceError102Index = index++;
	public static int cellArrayConformanceError103Index = index++;
	public static int cellArrayConformanceError104Index = index++;
	public static int cellArrayConformanceError105Index = index++;
	public static int cellArrayConformanceError106Index = index++;

	public static int conformanceErrorIndex = index++;

	public static int fixedInStage1Index = index++;

	public static File resFile = null;
	public static String category = null;
	public static TotalResultWriter curData = new TotalResultWriter();

	public static void init(File resFile, String category, boolean isDir)
			throws Exception {

		TotalResultWriter.resFile = resFile;
		TotalResultWriter.category = category;

		TotalResultWriter.curData = new TotalResultWriter();

		// initialize the file when we start to handle a new category.
		if (isDir) {
			ProcessLock lock = new ProcessLock();
			lock.lock();

			if (!resFile.exists()) {
				InputStream in = TotalResultWriter.class.getClassLoader()
						.getResourceAsStream("resources/results.xls");
				Workbook workbook = WorkbookFactory.create(in);
				workbook.write(new FileOutputStream(resFile));
				in.close();
				workbook.close();
			}

			// clean the data of category.
			cleanTotalResult();

			lock.unlock();
		}
	}

	public static void saveTotalResult() {
		ProcessLock lock = new ProcessLock();
		lock.lock();

		try {
			Workbook workbook = WorkbookFactory.create(new FileInputStream(
					resFile));
			Sheet sheet = workbook.getSheetAt(0);
			Row row = getRow(sheet, category);

			saveResult(row, timeIndex, (int) curData.time);

			saveResult(row, formulaIndex, curData.formula);

			saveResult(row, excelIndex, curData.excel);
			saveResult(row, excelProcessedIndex, curData.excelProcessed);
			saveResult(row, excelHasFormulaIndex, curData.excelHasFormula);
			saveResult(row, excelHasCellArrayIndex, curData.excelHasCellArray);
			saveResult(row, excelHasSmellyCellArrayIndex,
					curData.excelHasSmellyCellArray);
			saveResult(row, excelHasMissingFormulaCellArrayIndex,
					curData.excelHasMissingFormulaCellArray);
			saveResult(row, excelHasInconsistentFormulaCellArrayIndex,
					curData.excelHasInconsistentFormulaCellArray);

			saveResult(row, worksheetIndex, curData.worksheet);
			saveResult(row, worksheetHasFormulaIndex,
					curData.worksheetHasFormula);
			saveResult(row, worksheetHasCellArrayIndex,
					curData.worksheetHasCellArray);
			saveResult(row, worksheetHasSmellyCellArrayIndex,
					curData.worksheetHasSmellyCellArray);
			saveResult(row, worksheetHasMissingFormulaCellArrayIndex,
					curData.worksheetHasMissingFormulaCellArray);
			saveResult(row, worksheetHasInconsistentFormulaCellArrayIndex,
					curData.worksheetHasInconsistentFormulaCellArray);

			saveResult(row, cellArrayIndex, curData.cellArray);
			saveResult(row, cellArraySameRowOrColumnIndex,
					curData.cellArraySameRowOrColumn);
			saveResult(row, cellArrayFullRowOrColumnIndex,
					curData.cellArrayFullRowOrColumn);
			saveResult(row, cellArrayHasConstantsIndex,
					curData.cellArrayHasConstants);
			saveResult(row, oppositeCellArrayIndex, curData.oppositeCellArray);
			saveResult(row, overlapCellArrayIndex, curData.overlapCellArray);
			saveResult(row, allOverlapCellArrayIndex,
					curData.allOverlapCellArray);
			saveResult(row, cellArray2Index, curData.cellArray2);
			saveResult(row, cellArray2SameRowOrColumnIndex,
					curData.cellArray2SameRowOrColumn);
			saveResult(row, cellArray2FullRowOrColumnIndex,
					curData.cellArray2FullRowOrColumn);
			saveResult(row, correctCellArrayIndex, curData.correctCellArray);
			saveResult(row, correctCellArraySameRowOrColumnIndex,
					curData.correctCellArraySameRowOrColumn);
			saveResult(row, smellyCellArrayIndex, curData.smellyCellArray);
			saveResult(row, smellyCellArraySameRowOrColumnIndex,
					curData.smellyCellArraySameRowOrColumn);
			saveResult(row, cellArrayMissingFormulaIndex,
					curData.cellArrayMissingFormula);
			saveResult(row, cellArrayInconsistentFormulaIndex,
					curData.cellArrayInconsistentFormula);
			saveResult(row, cellArrayBothSmellIndex,
					curData.cellArrayMissingFormula
							+ curData.cellArrayInconsistentFormula
							- curData.smellyCellArray);

			saveResult(row, cellArrayConformanceError100Index,
					curData.cellArrayConformanceError100);
			saveResult(row, cellArrayConformanceError100ReferIndex,
					curData.cellArrayConformanceError100Refer);
			saveResult(row, cellArrayConformanceError101Index,
					curData.cellArrayConformanceError101);
			saveResult(row, cellArrayConformanceError102Index,
					curData.cellArrayConformanceError102);
			saveResult(row, cellArrayConformanceError103Index,
					curData.cellArrayConformanceError103);
			saveResult(row, cellArrayConformanceError104Index,
					curData.cellArrayConformanceError104);
			saveResult(row, cellArrayConformanceError105Index,
					curData.cellConformanceError105);
			saveResult(row, cellArrayConformanceError106Index,
					curData.cellArrayConformanceError106);

			saveResult(row, conformanceErrorIndex, curData.conformanceError);

			saveResult(row, fixedInStage1Index, curData.fixedInStage1);

			workbook.write(new FileOutputStream(resFile));
			workbook.close();
		} catch (Exception e) {
			Log.logNewLine(e, Log.writer);
		}

		lock.unlock();
	}

	private static void cleanTotalResult() {
		try {
			Workbook workbook = WorkbookFactory.create(new FileInputStream(
					resFile));
			Sheet sheet = workbook.getSheetAt(0);
			Row row = getRow(sheet, category);

			cleanResult(row, timeIndex);

			cleanResult(row, formulaIndex);

			cleanResult(row, excelIndex);
			cleanResult(row, excelProcessedIndex);
			cleanResult(row, excelHasFormulaIndex);
			cleanResult(row, excelHasCellArrayIndex);
			cleanResult(row, excelHasSmellyCellArrayIndex);
			cleanResult(row, excelHasMissingFormulaCellArrayIndex);
			cleanResult(row, excelHasInconsistentFormulaCellArrayIndex);

			cleanResult(row, worksheetIndex);
			cleanResult(row, worksheetHasFormulaIndex);
			cleanResult(row, worksheetHasCellArrayIndex);
			cleanResult(row, worksheetHasSmellyCellArrayIndex);
			cleanResult(row, worksheetHasMissingFormulaCellArrayIndex);
			cleanResult(row, worksheetHasInconsistentFormulaCellArrayIndex);

			cleanResult(row, cellArrayIndex);
			cleanResult(row, cellArraySameRowOrColumnIndex);
			cleanResult(row, cellArrayFullRowOrColumnIndex);
			cleanResult(row, cellArrayHasConstantsIndex);
			cleanResult(row, oppositeCellArrayIndex);
			cleanResult(row, overlapCellArrayIndex);
			cleanResult(row, allOverlapCellArrayIndex);
			cleanResult(row, cellArray2Index);
			cleanResult(row, cellArray2SameRowOrColumnIndex);
			cleanResult(row, cellArray2FullRowOrColumnIndex);
			cleanResult(row, correctCellArrayIndex);
			cleanResult(row, correctCellArraySameRowOrColumnIndex);
			cleanResult(row, smellyCellArrayIndex);
			cleanResult(row, smellyCellArraySameRowOrColumnIndex);
			cleanResult(row, cellArrayMissingFormulaIndex);
			cleanResult(row, cellArrayInconsistentFormulaIndex);
			cleanResult(row, cellArrayBothSmellIndex);

			cleanResult(row, cellArrayConformanceError100Index);
			cleanResult(row, cellArrayConformanceError100ReferIndex);
			cleanResult(row, cellArrayConformanceError101Index);
			cleanResult(row, cellArrayConformanceError102Index);
			cleanResult(row, cellArrayConformanceError103Index);
			cleanResult(row, cellArrayConformanceError104Index);
			cleanResult(row, cellArrayConformanceError105Index);
			cleanResult(row, cellArrayConformanceError106Index);

			cleanResult(row, conformanceErrorIndex);

			cleanResult(row, fixedInStage1Index);

			workbook.write(new FileOutputStream(resFile));
			workbook.close();
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}

	private static void saveResult(Row row, int index, int data) {
		Cell cell = row.getCell(index);
		if (cell == null) {
			cell = row.createCell(index);
		}
		int value = (int) cell.getNumericCellValue();
		cell.setCellValue(value + data);
	}

	private static int getResult(Row row, int index) {
		Cell cell = row.getCell(index);
		if (cell == null) {
			return 0;
		}
		return (int) cell.getNumericCellValue();
	}

	private static void cleanResult(Row row, int index) {
		Cell cell = row.getCell(index);
		if (cell == null) {
			cell = row.createCell(index);
		}
		cell.setCellValue(0);
	}

	public static void printTotalResult(BufferedWriter writer) {
		try {
			Workbook workbook = WorkbookFactory.create(new FileInputStream(
					resFile));
			Sheet sheet = workbook.getSheetAt(0);
			Row row = getRow(sheet, category);

			Log.logNewLine(writer);

			Log.logNewLine("Time: " + getResult(row, timeIndex) + "s", writer);

			Log.logNewLine("Formula: " + getResult(row, formulaIndex), writer);

			Log.logNewLine("Excel: " + getResult(row, excelIndex), writer);
			Log.logNewLine(
					"Processed Excel: " + getResult(row, excelProcessedIndex),
					writer);
			Log.logNewLine(
					"Excel Have Formula: "
							+ getResult(row, excelHasFormulaIndex), writer);
			Log.logNewLine(
					"Excel Have CellArray: "
							+ getResult(row, excelHasCellArrayIndex), writer);
			Log.logNewLine(
					"Excel Have Smelly CellArray: "
							+ getResult(row, excelHasSmellyCellArrayIndex),
					writer);
			Log.logNewLine("Excel Have Missing Formula CellArray: "
					+ getResult(row, excelHasMissingFormulaCellArrayIndex),
					writer);
			Log.logNewLine(
					"Excel Have Inconsistent Formula CellArray: "
							+ getResult(row,
									excelHasInconsistentFormulaCellArrayIndex),
					writer);

			Log.logNewLine("Worksheet: " + getResult(row, worksheetIndex),
					writer);
			Log.logNewLine(
					"Worksheet Have Formula: "
							+ getResult(row, worksheetHasFormulaIndex), writer);
			Log.logNewLine(
					"Worksheet Have CellArray: "
							+ getResult(row, worksheetHasCellArrayIndex),
					writer);
			Log.logNewLine(
					"Worksheet Have Smelly CellArray: "
							+ getResult(row, worksheetHasSmellyCellArrayIndex),
					writer);
			Log.logNewLine("Worksheet Have Missing Fomula CellArray: "
					+ getResult(row, worksheetHasMissingFormulaCellArrayIndex),
					writer);
			Log.logNewLine(
					"Worksheet Have Inconsistent Formula CellArray: "
							+ getResult(row,
									worksheetHasInconsistentFormulaCellArrayIndex),
					writer);

			Log.logNewLine("CellArray: " + getResult(row, cellArrayIndex),
					writer);
			Log.logNewLine(
					"CellArray with same row or column: "
							+ getResult(row, cellArraySameRowOrColumnIndex),
					writer);
			Log.logNewLine(
					"CellArray has constants: "
							+ getResult(row, cellArrayHasConstantsIndex),
					writer);
			Log.logNewLine(
					"CellArray has opposite CA: "
							+ getResult(row, oppositeCellArrayIndex), writer);
			Log.logNewLine(
					"Overlaped cell arrays:"
							+ getResult(row, overlapCellArrayIndex), writer);
			Log.logNewLine(
					"All overlaped cell arrays:"
							+ getResult(row, allOverlapCellArrayIndex), writer);
			Log.logNewLine(
					"Correct CellArray: "
							+ getResult(row, correctCellArrayIndex), writer);
			Log.logNewLine(
					"Smelly CellArray: " + getResult(row, smellyCellArrayIndex),
					writer);
			Log.logNewLine(
					"CellArray with Missing Formula: "
							+ getResult(row, cellArrayMissingFormulaIndex),
					writer);
			Log.logNewLine(
					"CellArray with Inconsistent Formula: "
							+ getResult(row, cellArrayInconsistentFormulaIndex),
					writer);
			Log.logNewLine(
					"CellArray with Both Smells: "
							+ (getResult(row, cellArrayMissingFormulaIndex)
									+ getResult(row,
											cellArrayInconsistentFormulaIndex) - getResult(
										row, smellyCellArrayIndex)), writer);

			Log.logNewLine(
					"CellArray with 100% correct: "
							+ getResult(row, cellArrayConformanceError100Index),
					writer);
			Log.logNewLine(
					"CellArray with 90% correct: "
							+ getResult(row, cellArrayConformanceError101Index),
					writer);
			Log.logNewLine(
					"CellArray with 80% correct: "
							+ getResult(row, cellArrayConformanceError102Index),
					writer);
			Log.logNewLine(
					"CellArray with 70% correct: "
							+ getResult(row, cellArrayConformanceError103Index),
					writer);
			Log.logNewLine(
					"CellArray with 60% correct: "
							+ getResult(row, cellArrayConformanceError104Index),
					writer);
			Log.logNewLine(
					"CellArray with 50% correct: "
							+ getResult(row, cellArrayConformanceError105Index),
					writer);
			Log.logNewLine(
					"CellArray with 00% correct: "
							+ getResult(row, cellArrayConformanceError106Index),
					writer);

			Log.logNewLine(
					"Conformance Errors: "
							+ getResult(row, conformanceErrorIndex), writer);

			Log.logNewLine(
					"Fixed In Stage 1: " + getResult(row, fixedInStage1Index),
					writer);
		} catch (Exception e) {
			Log.logNewLine(e, Log.writer);
		}
	}

	private static Row getRow(Sheet sheet, String category) {
		Iterator<Row> rowIterator = sheet.iterator();
		while (rowIterator.hasNext()) {
			Row row = rowIterator.next();
			Cell cell = row.getCell(0);
			if (cell != null && cell.getStringCellValue().equals(category)) {
				return row;
			}
		}

		// can't find it, create one.
		Row row = sheet.getRow(categoryRowIndex);
		if (row == null) {
			row = sheet.createRow(categoryRowIndex);
		}
		categoryRowIndex++;

		row.createCell(0).setCellValue(category);
		return row;
	}

	public static void addCAResult(List<CAResult> allCARs, AMSheet sheet,
			BufferedWriter writer) {
		// analyze overlap cell arrays.
		CAOverlap.analyzeOverlap(allCARs);

		for (CAResult car : allCARs) {
			addCAResult(car, sheet);
		}
	}

	private static void addCAResult(CAResult car, AMSheet sheet) {
		TotalResultWriter.curData.cellArray++;
		if (car.isAmbiguous) {
			TotalResultWriter.curData.smellyCellArray++;
		} else {
			TotalResultWriter.curData.correctCellArray++;
		}

		if (car.isSameRowOrColumn) {
			TotalResultWriter.curData.cellArraySameRowOrColumn++;
			if (car.isAmbiguous) {
				TotalResultWriter.curData.smellyCellArraySameRowOrColumn++;
			} else {
				TotalResultWriter.curData.correctCellArraySameRowOrColumn++;
			}
		}
		if (car.isFullRowOrColumn) {
			TotalResultWriter.curData.cellArrayFullRowOrColumn++;
		}
		if (car.hasConstants) {
			TotalResultWriter.curData.cellArrayHasConstants++;
		}
		if (car.isOpposite()) {
			TotalResultWriter.curData.oppositeCellArray++;
		}
		if (car.isOverlap) {
			TotalResultWriter.curData.overlapCellArray++;
		}
		if (car.isAllOverlap) {
			TotalResultWriter.curData.allOverlapCellArray++;
		}
		
		TotalResultWriter.curData.cellArray2++;
		if (car.isSameRowOrColumn) {
			TotalResultWriter.curData.cellArray2SameRowOrColumn++;
		}
		if (car.isFullRowOrColumn) {
			TotalResultWriter.curData.cellArray2FullRowOrColumn++;
		}

		if (car.isMissing) {
			TotalResultWriter.curData.cellArrayMissingFormula++;
		}
		if (car.isInconsistent) {
			TotalResultWriter.curData.cellArrayInconsistentFormula++;
		}

		if (car.isAmbiguous) {
			TotalResultWriter.curData.conformanceError += car.errorCells.size();
		}

		if (car.isFixInStage1()) {
			TotalResultWriter.curData.fixedInStage1++;
		}

		if (!car.isAmbiguous) {
			if (car.referChangedCells.size() > 0) {
				TotalResultWriter.curData.cellArrayConformanceError100Refer++;
			}
		}

		if (car.isAmbiguous) {

			double percentage = car.percentage;

			if (percentage >= 1.0) {
				TotalResultWriter.curData.cellArrayConformanceError100++;
			} else if (percentage >= 0.9 && percentage < 1.0) {
				TotalResultWriter.curData.cellArrayConformanceError101++;
			} else if (percentage >= 0.8 && percentage < 0.9) {
				TotalResultWriter.curData.cellArrayConformanceError102++;
			} else if (percentage >= 0.7 && percentage < 0.8) {
				TotalResultWriter.curData.cellArrayConformanceError103++;
			} else if (percentage >= 0.6 && percentage < 0.7) {
				TotalResultWriter.curData.cellArrayConformanceError104++;
			} else if (percentage >= 0.5 && percentage < 0.6) {
				TotalResultWriter.curData.cellConformanceError105++;
			} else {
				TotalResultWriter.curData.cellArrayConformanceError106++;
			}
		}
	}
}
