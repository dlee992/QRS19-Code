package thirdparty.CACheck.amcheck;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import thirdparty.CACheck.AMSheet;
import thirdparty.CACheck.CellArray;
import thirdparty.CACheck.cellarray.extract.*;
import thirdparty.CACheck.cellarray.inference.ConstraintGenerator;
import thirdparty.CACheck.cellarray.inference.FormulaInference;
import thirdparty.CACheck.snippet.ExtractSnippet;
import thirdparty.CACheck.snippet.Snippet;
import thirdparty.CACheck.util.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ExcelAnalysis {

	public static void main(String[] args) throws Exception {
        String logFile = "C:\\Users\\Yolanda\\Desktop\\log.txt";
        BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true));
        Log.writer = writer;

        AnalysisPattern analysisPattern = new AnalysisPattern();
		analysisPattern.setType(11);

		File xlsFile = new File("C:\\Users\\Yolanda\\Desktop\\test.xls");
		Workbook workbook = WorkbookFactory.create(new FileInputStream(xlsFile));
		Sheet st = workbook.getSheetAt(0);
		List<CAResult> allCARs = new ArrayList<CAResult>();
		AMSheet sheet = Utils.extractSheet(st, xlsFile.getName());

		ExtractSnippet extractSnippet = new ExtractSnippet(sheet);
		List<Snippet> snippets = extractSnippet.extractSnippet();
		for (Snippet snippet : snippets) {
			List<CAResult> tmp = processSnippet(xlsFile.getName(), sheet,
					snippet, snippets, writer, analysisPattern);
			allCARs.addAll(tmp);
		}

		SpreadsheetMark.markDetectResult(sheet, allCARs);

		File outFilepath = new File("C:\\Users\\Yolanda\\Desktop\\testOutput.xls");
		FileOutputStream outFile = new FileOutputStream(outFilepath);
		workbook.write(outFile);
		outFile.close();
		workbook.close();
	}



	public static void processExcel(String category, File xlsFile,
			String outDir, BufferedWriter writer, AnalysisPattern analysisPattern) {

		StringBuffer sb = new StringBuffer();
		sb.append("[");
		sb.append(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
				.format(new Date()));
		sb.append("]  ");
		sb.append("Current file: ");
		sb.append(category + "-");
		sb.append(xlsFile.getName());
		System.err.println(sb.toString());

		List<CAResult> smells = new ArrayList<CAResult>();

		Workbook workbook = null;
		try {
			Log.logNewLine(
					"########Process '" + category + "/" + xlsFile.getName()
							+ "'########", writer);

			workbook = WorkbookFactory.create(new FileInputStream(xlsFile));

			boolean haveCellArray = false;
			boolean haveFaultyCellArray = false;
			boolean haveMissingFormulaCellArray = false;
			boolean haveFaultyFormulaCellArray = false;
			for (int j = 0; j < workbook.getNumberOfSheets(); j++) {
				Sheet st = workbook.getSheetAt(j);

				Log.logNewLine("----Sheet '" + st.getSheetName() + "'----",
						writer);

				if (ExcelPreProcess.countFormulas(st) == 0) {
					// no formula
					continue;
				}

				List<CAResult> allCARs = new ArrayList<CAResult>();
				AMSheet sheet = Utils.extractSheet(st, xlsFile.getName());

				ExtractSnippet extractSnippet = new ExtractSnippet(sheet);
				List<Snippet> snippets = extractSnippet.extractSnippet();
				for (Snippet snippet : snippets) {
					List<CAResult> tmp = processSnippet(xlsFile.getName(), sheet,
							snippet, snippets, writer, analysisPattern);
					allCARs.addAll(tmp);
				}

				SpreadsheetMark.markDetectResult(sheet, allCARs);

				// add cell array statistics.
				TotalResultWriter.addCAResult(allCARs, sheet, writer);
				DetailResultWriter.addOverlaps(allCARs);
				DetailResultWriter.addCAResult(allCARs);
				SmellyCellWriter.addCAResult(allCARs);

				List<CAResult> allSmells = new ArrayList<CAResult>();
				List<CAResult> missingSmells = new ArrayList<CAResult>();
				List<CAResult> inConSmells = new ArrayList<CAResult>();
				for (CAResult car : allCARs) {
					if (car.isAmbiguous) {
						allSmells.add(car);
					}
					if (car.isMissing) {
						missingSmells.add(car);
					}
					if (car.isInconsistent) {
						inConSmells.add(car);
					}
				}
				smells.addAll(allSmells);

				if (allCARs.size() > 0) {
					TotalResultWriter.curData.worksheetHasCellArray++;
					haveCellArray = true;
				}
				if (allSmells.size() > 0) {
					TotalResultWriter.curData.worksheetHasSmellyCellArray++;
					haveFaultyCellArray = true;
				}

				if (missingSmells.size() > 0) {
					TotalResultWriter.curData.worksheetHasMissingFormulaCellArray++;
					haveMissingFormulaCellArray = true;
				}
				if (inConSmells.size() > 0) {
					TotalResultWriter.curData.worksheetHasInconsistentFormulaCellArray++;
					haveFaultyFormulaCellArray = true;
				}
			}

			if (haveCellArray) {
				TotalResultWriter.curData.excelHasCellArray++;
			}
			if (haveFaultyCellArray) {
				TotalResultWriter.curData.excelHasSmellyCellArray++;
			}
			if (haveMissingFormulaCellArray) {
				TotalResultWriter.curData.excelHasMissingFormulaCellArray++;
			}
			if (haveFaultyFormulaCellArray) {
				TotalResultWriter.curData.excelHasInconsistentFormulaCellArray++;
			}

			File categoryOutDir = new File(outDir + category);
			if (!categoryOutDir.exists()) {
				categoryOutDir.mkdirs();
			}
			String outFilepath = categoryOutDir.getAbsolutePath() + "/"
					+ xlsFile.getName();
			FileOutputStream outFile = new FileOutputStream(outFilepath);
			workbook.write(outFile);
			outFile.close();
			workbook.close();
		} catch (Exception e) {
			try {
				if (workbook != null) {
					workbook.close();
				}
			} catch (Exception e2) {
			}
			Log.logNewLine(e, writer);
		}
	}

	public static List<CAResult> processSnippet(String filename, AMSheet sheet,
			Snippet snippet, List<Snippet> allSnippets, BufferedWriter writer, AnalysisPattern analysisPattern)
	throws ArrayIndexOutOfBoundsException {

		Log.logNewLine("Processing snippet: " + snippet.toString(), writer);

		List<CAResult> allCARs = new ArrayList<CAResult>();
		try {
			CellArrayExtractor cae = CellArrayExtractor
					.getCellArrayExtractor(sheet, analysisPattern);
			List<CellArray> allCAs = cae.extractCellArray(snippet);
			if (allCAs.isEmpty()) {
				Log.logNewLine("Finish snippet: " + snippet.toString(), writer);
				return new ArrayList<CAResult>();
			}

			FormulaInference fs = new FormulaInference(sheet);
			SmellyCellArrayIdentification smellyCAExtract = new SmellyCellArrayIdentification(
					sheet);
			for (CellArray ca : allCAs) {
				CAResult car = new CAResult();
				allCARs.add(car);

				car.excel = filename;
				car.worksheet = sheet.getSheet().getSheetName();
				car.cellArray = ca;
				car.constraints = ConstraintGenerator.generateConstraints(
						sheet, ca);

				if (smellyCAExtract.isMissingFormulaSmell(ca)) {
					car.isMissing = true;
				}
				if (smellyCAExtract.isInconsistentFormulaSmell(car.constraints
						.getFormulaPatterns())) {
					car.isInconsistent = true;
				}
				if (car.isMissing || car.isInconsistent) {
					car.isAmbiguous = true;
				}
				if (!car.isAmbiguous) {
					if (car.constraints.getFormulaPatterns().size() != 0) {
						List<Object> pattern = car.constraints
								.getFormulaPatterns().get(0).pattern;
						AmbiguousDetector amDetector = new AmbiguousDetector(
								sheet, allSnippets);
						car.isSameRowOrColumn = amDetector.isSameRowOrColumn(
								ca, pattern);
					}
				}
			}

			CellArrayExtractor ccae = new CorrectCellArrayExtractor(sheet, analysisPattern);
			List<CellArray> corCAs = ccae.extractCellArray(snippet);
			List<CAResult> corCARs = new ArrayList<CAResult>();
			for (CellArray ca : corCAs) {
				CAResult car = new CAResult();
				corCARs.add(car);

				car.excel = filename;
				car.worksheet = sheet.getSheet().getSheetName();
				car.cellArray = ca;
				car.constraints = ConstraintGenerator.generateConstraints(
						sheet, ca);

				car.isMissing = false;
				car.isInconsistent = false;
				car.isAmbiguous = false;

				if (car.constraints.getFormulaPatterns().size() != 0) {
					List<Object> pattern = car.constraints.getFormulaPatterns()
							.get(0).pattern;
					AmbiguousDetector amDetector = new AmbiguousDetector(sheet,
							allSnippets);
					car.isSameRowOrColumn = amDetector.isSameRowOrColumn(ca,
							pattern);
					car.pattern = pattern;
				}
			}

			Log.logNewLine("*****PreProcess Filter*****", writer);
			List<CAResult> afterFilter1 = CellArrayFilter.preProcessFilter(
					sheet, allCARs, corCARs, analysisPattern);
			CellArrayFilter
					.printFilterCellArrays(allCARs, afterFilter1, writer);
			allCARs = afterFilter1;

			for (int i = 0; i < allCARs.size(); i++) {
				CAResult car = allCARs.get(i);
				try {
					fs.synthesis(car, writer);
				} catch (Exception e) {
					Log.logNewLine(e, writer);
					allCARs.remove(i);
					i--;
				}
			}
			// Refine cell arrays with common data cells.
			FixOverlapDataCell.fix(sheet, allCARs, analysisPattern);

			for (int i = 0; i < allCARs.size(); i++) {
				CAResult car = allCARs.get(i);
				try {
					if (car.pattern != null) {
						AmbiguousDetector amDetector = new AmbiguousDetector(
								sheet, allSnippets);
						amDetector.detect(car, writer, analysisPattern);
					}
				} catch (Exception e) {
					Log.logNewLine(e, writer);
					allCARs.remove(i);
					i--;
				}
			}

			Log.logNewLine("*****PostProcess Filter*****", writer);

			List<CAResult> afterFilter2 = CellArrayFilter.postProcessFilter(
					allCARs, corCAs, analysisPattern);

			CellArrayFilter
					.printFilterCellArrays(allCARs, afterFilter2, writer);
			allCARs = afterFilter2;

			// Recover compensate cell arrays with common data cells.
			FixOverlapDataCell.recover(sheet, allCARs, allSnippets, writer, analysisPattern);
		} catch (Exception e) {
			Log.logNewLine(e, writer);
		}

		// compute all consecutive formula cells, and judge whether it is a cell
		// array
		for (CAResult car : allCARs) {
			Cell preCell = null;
			Cell posCell = null;
			car.isFullRowOrColumn = true;
			if (car.cellArray.isRowCA) {
				int row = car.cellArray.rowOrColumn;
				if (car.cellArray.start != snippet.left) {
					int col = car.cellArray.start - 1;
					preCell = sheet.getCell(row, col);
				}
				if (car.cellArray.end != snippet.right) {
					int col = car.cellArray.end + 1;
					posCell = sheet.getCell(row, col);
				}
			} else {
				int col = car.cellArray.rowOrColumn;
				if (car.cellArray.start != snippet.up) {
					int row = car.cellArray.start - 1;
					preCell = sheet.getCell(row, col);
				} else if (car.cellArray.end != snippet.bottom) {
					int row = car.cellArray.end + 1;
					posCell = sheet.getCell(row, col);
				}
			}

			if (preCell != null) {
				if (CellUtils.isFormula(preCell) || CellUtils.isNumber(preCell)
						|| CellUtils.isDate(preCell)) {
					car.isFullRowOrColumn = false;
				}
			}
			if (posCell != null) {
				if (CellUtils.isFormula(posCell) || CellUtils.isNumber(posCell)
						|| CellUtils.isDate(posCell)) {
					car.isFullRowOrColumn = false;
				}
			}

			// TODO Measure whether neibouring cells share dependence. Delete
			// when measure others.
			car.isFullRowOrColumn = true;
			if (shareDependence(sheet, car.cellArray, preCell, posCell, analysisPattern)) {
				car.isFullRowOrColumn = false;
			}
		}

		return allCARs;
	}

	private static boolean shareDependence(AMSheet sheet, CellArray ca,
			Cell preCell, Cell posCell, AnalysisPattern analysisPattern) {
		try {
			if (preCell != null && CellUtils.isFormula(preCell)) {
				CellArray newCA = new CellArray(ca.rowOrColumn, ca.isRowCA,
						ca.start - 1, ca.end);
				DependenceCellArraySharedExtractor dcse = new DependenceCellArraySharedExtractor(
						sheet, analysisPattern);
				List<Dependence> deps = DependenceConstantExtractor
						.constructDependence(sheet, newCA);
				if (dcse.isCellArray(newCA, deps)) {
					return true;
				}
			}
			if (posCell != null && CellUtils.isFormula(posCell)) {
				CellArray newCA = new CellArray(ca.rowOrColumn, ca.isRowCA,
						ca.start, ca.end + 1);
				DependenceCellArraySharedExtractor dcse = new DependenceCellArraySharedExtractor(
						sheet, analysisPattern);
				List<Dependence> deps = DependenceConstantExtractor
						.constructDependence(sheet, newCA);
				if (dcse.isCellArray(newCA, deps)) {
					return true;
				}
			}
		} catch (Exception e) {
		}

		return false;
	}
}
