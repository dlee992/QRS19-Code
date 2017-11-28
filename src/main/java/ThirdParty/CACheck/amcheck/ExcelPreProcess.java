package ThirdParty.CACheck.amcheck;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import ThirdParty.CACheck.util.CellUtils;
import ThirdParty.CACheck.util.Log;
import ThirdParty.CACheck.util.TotalResultWriter;

public class ExcelPreProcess {

	public static File[] getAllExcels(File inDir) throws IOException {

		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File directory, String fileName) {
				return fileName.toLowerCase().endsWith(".xls")
						|| fileName.toLowerCase().endsWith(".xlsx");
			}
		};

		File[] files = inDir.listFiles(filter);

		List<File> source = new ArrayList<File>();
		for (File f : files) {
			source.add(f);
		}

		List<File> res = new ArrayList<File>();
		boolean refined = AnalysisPattern.refined;
		while (source.size() > 0) {
			File file = source.remove(0);
			String fName = file.getName();
			if (fName.startsWith("refined__")) {
				File originalFile = null;
				for (int i = 0; i < source.size(); i++) {
					File tmpFile = source.get(i);
					String tmpFileName = tmpFile.getName();
					if (tmpFileName.equals(fName.substring(9))) {
						originalFile = tmpFile;
						break;
					}
				}
				if (refined) {
					res.add(file);
				} else {
					res.add(originalFile);
				}
				source.remove(originalFile);
			} else {
				File refineFile = null;
				for (int i = 0; i < source.size(); i++) {
					File tmpFile = source.get(i);
					String tmpFileName = tmpFile.getName();
					if (tmpFileName.equals("refined__" + fName)) {
						refineFile = tmpFile;
						break;
					}
				}
				if (refined && refineFile != null) {
					res.add(refineFile);
				} else {
					res.add(file);
				}
				if (refineFile != null) {
					source.remove(refineFile);
				}
			}
		}

		return res.toArray(new File[0]);
	}

	public static boolean preProcess(File file) {
		Workbook workbook = null;

		try {
			TotalResultWriter.curData.excel += 1;

			workbook = WorkbookFactory.create(new FileInputStream(file));

			boolean hasFormula = false;
			int totalFormula = 0;
			int worksheetHasFormula = 0;
			for (int j = 0; j < workbook.getNumberOfSheets(); j++) {
				Sheet st = workbook.getSheetAt(j);
				int formulas = countFormulas(st);

				if (formulas > 0) {
					hasFormula = true;
					totalFormula += formulas;
					worksheetHasFormula++;
				}
			}

			TotalResultWriter.curData.worksheet += workbook.getNumberOfSheets();
			TotalResultWriter.curData.formula += totalFormula;
			TotalResultWriter.curData.worksheetHasFormula += worksheetHasFormula;
			TotalResultWriter.curData.excelProcessed++;
			if (hasFormula) {
				TotalResultWriter.curData.excelHasFormula++;
			}

			workbook.close();

			if (hasFormula) {
				return true;
			}
		} catch (Exception e) {
			System.err.println("Unprocessed file: " + file.getName());
			Log.logNewLine("Unprocessed file: " + file.getName(), Log.writer);

			if (workbook != null) {
				try {
					workbook.close();
				} catch (Exception e1) {
				}
			}
		}
		return false;
	}

	public static int countFormulas(Sheet sheet) throws IOException {
		int numOfFormula = 0;

		Iterator<Row> rowIterator = sheet.iterator();
		while (rowIterator.hasNext()) {
			Row row = rowIterator.next();
			Iterator<Cell> cellIterator = row.cellIterator();
			while (cellIterator.hasNext()) {
				Cell cell = cellIterator.next();
				if (cell != null
						&& CellUtils.isFormula(cell)) {
					// try to get the formula, may throw
					// exceptions.
					cell.getCellFormula();
					numOfFormula++;
				}
			}
		}

		return numOfFormula;
	}
}
